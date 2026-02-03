package no.nav.dokdistdpv.qdist016.altinn3;

import lombok.extern.slf4j.Slf4j;
import no.altinn.services.altinn3.openapi.domain.AttachmentOverviewExt;
import no.nav.dokdistdpv.cloudstorage.DokDistDokumentFraBucket;
import no.nav.dokdistdpv.cloudstorage.EncryptedBucketStorage;
import no.nav.dokdistdpv.consumer.altinn3.Altinn3CorrespondenceClient;
import no.nav.dokdistdpv.qdist016.altinn3.map.InitializeAttachmentMapper;
import no.nav.dokdistdpv.qdist016.dokument.NavDokument;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Altinn3AttachmentUploadService {
	private final Altinn3CorrespondenceClient altinn3CorrespondenceClient;
	private final EncryptedBucketStorage storage;

	Altinn3AttachmentUploadService(Altinn3CorrespondenceClient altinn3CorrespondenceClient,
								   EncryptedBucketStorage storage) {
		this.altinn3CorrespondenceClient = altinn3CorrespondenceClient;
		this.storage = storage;
	}

	public UploadedAttachment upload(String bestillingsId, NavDokument navDokument) {
		String dokumentObjektReferanse = navDokument.dokumentObjektReferanse();
		DokDistDokumentFraBucket dokDistDokumentFraBucket = storage.downloadObject(dokumentObjektReferanse, bestillingsId);
		String md5Hex = DigestUtils.md5Hex(dokDistDokumentFraBucket.getPdf());
		String attachmentId = altinn3CorrespondenceClient.initializeAttachment(InitializeAttachmentMapper.map(navDokument, md5Hex));
		AttachmentOverviewExt attachmentOverviewExt = altinn3CorrespondenceClient.uploadAttachment(attachmentId, dokDistDokumentFraBucket.getPdf());
		log.info("Lastet opp attachment til Altinn3. attachmentId={}, sendersReference={}, length={}",
				attachmentOverviewExt.getAttachmentId(), attachmentOverviewExt.getSendersReference(), dokDistDokumentFraBucket.getPdf().length);
		return new UploadedAttachment(attachmentId, navDokument.rekkefoelge());
	}
}
