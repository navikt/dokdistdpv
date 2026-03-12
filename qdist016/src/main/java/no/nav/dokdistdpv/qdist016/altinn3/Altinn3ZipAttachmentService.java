package no.nav.dokdistdpv.qdist016.altinn3;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import no.altinn.services.altinn3.openapi.domain.AttachmentOverviewExt;
import no.altinn.services.altinn3.openapi.domain.InitializeAttachmentExt;
import no.nav.dokdistdpv.cloudstorage.DokDistDokumentFraBucket;
import no.nav.dokdistdpv.cloudstorage.EncryptedBucketStorage;
import no.nav.dokdistdpv.consumer.altinn3.Altinn3CorrespondenceClient;
import no.nav.dokdistdpv.exception.AltinnException;
import no.nav.dokdistdpv.exception.DokdistdpvTechnicalException;
import no.nav.dokdistdpv.qdist016.dokument.NavDokument;
import no.nav.dokdistdpv.qdist016.dokument.NavDokumenter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.channels.ByteArraySeekableByteChannel;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.StructuredTaskScope;

import static no.nav.dokdistdpv.consumer.altinn3.Altinn3Constants.NAV_RESOURCE_ID;
import static no.nav.dokdistdpv.qdist016.altinn3.map.InitializeAttachmentNameMapper.mapFileName;
import static org.apache.commons.io.IOUtils.SOFT_MAX_ARRAY_LENGTH;

@Slf4j
@Component
public class Altinn3ZipAttachmentService extends AbstractAltinn3AttachmentService {
	private static final String ZIP_FILENAME = "Taushetsbelagt Post fra Nav - alle vedlegg.zip";
	private static final String ZIP_DISPLAYNAME = "Taushetsbelagt Post fra Nav - alle vedlegg";
	private static final int CONCURRENT_FETCH_SIZE = 20;

	Altinn3ZipAttachmentService(Altinn3CorrespondenceClient altinn3CorrespondenceClient,
								EncryptedBucketStorage storage) {
		super(altinn3CorrespondenceClient, storage);
	}

	public List<UploadedAttachment> uploadAll(NavDokumenter navDokumenter) {
		if (navDokumenter.getSumVedleggFilstoerrelse() >= SOFT_MAX_ARRAY_LENGTH) {
			throw new AltinnException("Altinn3ZipAttachmentService støtter ikke zip større enn " + SOFT_MAX_ARRAY_LENGTH + " bytes");
		}
		UploadedAttachment hoveddokumentAttachment = fetchInitializeUpload(navDokumenter.getBestillingsId(), navDokumenter.getHoveddokument());
		UploadedAttachment zipAttachment = initializeUploadZippedVedlegg(navDokumenter);
		return List.of(hoveddokumentAttachment, zipAttachment);
	}

	private UploadedAttachment initializeUploadZippedVedlegg(NavDokumenter navDokumenter) {
		byte[] zip = createZip(navDokumenter);

		String zipMd5Hex = DigestUtils.md5Hex(zip);
		String zipAttachmentId = altinn3CorrespondenceClient.initializeAttachment(InitializeAttachmentExt.builder()
				.fileName(ZIP_FILENAME)
				.displayName(ZIP_DISPLAYNAME)
				.isEncrypted(false)
				.checksum(zipMd5Hex)
				.sendersReference(navDokumenter.getBestillingsId())
				.resourceId(NAV_RESOURCE_ID)
				.build());
		AttachmentOverviewExt attachmentOverviewExt = altinn3CorrespondenceClient.uploadAttachment(zipAttachmentId, zip);
		log.info("Lastet opp zip attachment til Altinn3. attachmentId={}, sendersReference={}, length={}",
				attachmentOverviewExt.getAttachmentId(), attachmentOverviewExt.getSendersReference(), zip.length);
		return new UploadedAttachment(zipAttachmentId, navDokumenter.getHoveddokument().rekkefoelge() + 1);
	}

	private byte[] createZip(NavDokumenter navDokumenter) {
		int sumVedleggFilstoerrelse = navDokumenter.getSumVedleggFilstoerrelse();
		log.info("Allokerer {} bytes til zip attachment for bestillingsId={}", sumVedleggFilstoerrelse, navDokumenter.getBestillingsId());
		final ByteArraySeekableByteChannel byteChannel = ByteArraySeekableByteChannel.wrap(new byte[sumVedleggFilstoerrelse]);
		try (ZipArchiveOutputStream zipOut = new ZipArchiveOutputStream(byteChannel)) {
			zipOut.setUseZip64(Zip64Mode.AsNeeded);
			List<List<NavDokument>> partition = Lists.partition(navDokumenter.getVedlegg(), CONCURRENT_FETCH_SIZE);
			partition.forEach(navDokumenterPartition -> {
				List<DokDistDokumentFraBucket> dokumenter = getDokumenter(navDokumenter.getBestillingsId(), navDokumenterPartition);
				dokumenter.forEach(dokDistDokumentFraBucket -> {
					NavDokument navDokument = navDokumenter.findByDokumentObjektReferanse(dokDistDokumentFraBucket.getObjectName());
					try {
						addEntryToZip(zipOut, mapFileName(navDokument), dokDistDokumentFraBucket.getPdf());
					} catch (IOException e) {
						throw new DokdistdpvTechnicalException("Klarte ikke legge fil dokumentObjektReferanse=" + navDokument.dokumentObjektReferanse() + " i zip. " + e.getMessage(), e);
					}
				});

			});
		} catch (IOException e) {
			throw new DokdistdpvTechnicalException("Klarte ikke lukke ZipArchiveOutputStream. " + e.getMessage(), e);
		}
		return byteChannel.toByteArray();
	}

	private List<DokDistDokumentFraBucket> getDokumenter(String bestillingsId, List<NavDokument> navDokumenter) {
		try (var scope = StructuredTaskScope.open(StructuredTaskScope.Joiner.<DokDistDokumentFraBucket>allSuccessfulOrThrow(),
				configuration -> configuration.withTimeout(Duration.ofMinutes(2)))) {
			navDokumenter.forEach(navDokument ->
					scope.fork(() -> storage.downloadObject(navDokument.dokumentObjektReferanse(), bestillingsId)));
			try {
				return scope.join().map(StructuredTaskScope.Subtask::get).toList();
			} catch (InterruptedException e) {
				throw new DokdistdpvTechnicalException("Henting av Nav dokumenter fra GCS avbrutt", e);
			}
		}
	}

	private static void addEntryToZip(ZipArchiveOutputStream zipOut, String entryName, byte[] buffer)
			throws IOException {
		ZipArchiveEntry archiveEntry = new ZipArchiveEntry(entryName);
		archiveEntry.setSize(buffer.length);
		zipOut.putArchiveEntry(archiveEntry);
		try (ByteArrayInputStream bis = new ByteArrayInputStream(buffer)) {
			IOUtils.copy(bis, zipOut);
		}
		zipOut.closeArchiveEntry();
	}
}
