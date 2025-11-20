package no.nav.dokdistdpv.qdist016.altinn3;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpv.consumer.altinn3.Altinn3CorrespondenceClient;
import no.nav.dokdistdpv.consumer.altinn3.api.correspondence.InitializeCorrespondencesExt;
import no.nav.dokdistdpv.consumer.altinn3.api.correspondence.InitializeCorrespondencesResponseExt;
import no.nav.dokdistdpv.consumer.altinn3.api.correspondence.InitializedCorrespondencesExt;
import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse;
import no.nav.dokdistdpv.exception.DokdistdpvTechnicalException;
import no.nav.dokdistdpv.qdist016.altinn3.map.InitializeCorrespondencesMapper;
import no.nav.dokdistdpv.qdist016.dokument.NavDokumentService;
import no.nav.dokdistdpv.qdist016.dokument.NavDokumenter;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.StructuredTaskScope;

@Slf4j
@Component
public class Altinn3MeldingService {
	private final NavDokumentService navDokumentService;
	private final Altinn3AttachmentUploadService altinn3AttachmentUploadService;
	private final Altinn3CorrespondenceClient altinn3CorrespondenceClient;

	public Altinn3MeldingService(NavDokumentService navDokumentService,
								 Altinn3AttachmentUploadService altinn3AttachmentUploadService,
								 Altinn3CorrespondenceClient altinn3CorrespondenceClient) {
		this.navDokumentService = navDokumentService;
		this.altinn3AttachmentUploadService = altinn3AttachmentUploadService;
		this.altinn3CorrespondenceClient = altinn3CorrespondenceClient;
	}

	public String distribuer(HentForsendelseResponse forsendelse) {
		NavDokumenter navDokumenter = navDokumentService.hentNavDokumenter(forsendelse);

		List<UploadedAttachment> attachmentIds = concurrentUpload(navDokumenter);
		InitializeCorrespondencesExt initializeCorrespondencesExt = InitializeCorrespondencesMapper.map(forsendelse, attachmentIds);
		InitializeCorrespondencesResponseExt initializeCorrespondencesResponseExt = altinn3CorrespondenceClient.initializeCorrespondence(initializeCorrespondencesExt);
		InitializedCorrespondencesExt correspondence = initializeCorrespondencesResponseExt.correspondences().getFirst();
		log.info("Sendt melding til virksomhet i Altinn3. correspondenceId={}, numAttachments={}",
				correspondence.correspondenceId(),
				attachmentIds.size());
		return initializeCorrespondencesResponseExt.getCorrespondenceId();
	}

	private List<UploadedAttachment> concurrentUpload(NavDokumenter navDokumenter) {
		try (var scope = StructuredTaskScope.open(StructuredTaskScope.Joiner.<UploadedAttachment>allSuccessfulOrThrow(),
				configuration -> configuration.withTimeout(Duration.ofMinutes(4)))) {
			navDokumenter.dokumenter().forEach(navDokument ->
					scope.fork(() -> altinn3AttachmentUploadService.upload(navDokumenter.bestillingsId(), navDokument)));
			try {
				return scope.join().map(StructuredTaskScope.Subtask::get).toList();
			} catch (InterruptedException e) {
				throw new DokdistdpvTechnicalException("Opplasting av Nav dokumenter til Altinn avbrutt", e);
			}
		}
	}
}
