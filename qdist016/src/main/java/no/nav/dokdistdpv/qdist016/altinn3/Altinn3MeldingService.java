package no.nav.dokdistdpv.qdist016.altinn3;

import lombok.extern.slf4j.Slf4j;
import no.altinn.services.altinn3.openapi.domain.InitializeCorrespondencesExt;
import no.altinn.services.altinn3.openapi.domain.InitializeCorrespondencesResponseExt;
import no.altinn.services.altinn3.openapi.domain.InitializedCorrespondencesExt;
import no.nav.dokdistdpv.consumer.altinn3.Altinn3CorrespondenceClient;
import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse;
import no.nav.dokdistdpv.properties.DokdistdpvProperties;
import no.nav.dokdistdpv.qdist016.altinn3.map.InitializeCorrespondencesMapper;
import no.nav.dokdistdpv.qdist016.dokument.NavDokumentService;
import no.nav.dokdistdpv.qdist016.dokument.NavDokumenter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class Altinn3MeldingService {
	private final Altinn3CorrespondenceClient altinn3CorrespondenceClient;
	private final NavDokumentService navDokumentService;
	private final Altinn3DefaultAttachmentService altinn3DefaultAttachmentService;
	private final Altinn3ZipAttachmentService altinn3ZipAttachmentService;
	private final int altinn3VedleggZipGrense;

	public Altinn3MeldingService(DokdistdpvProperties dokdistdpvProperties,
								 NavDokumentService navDokumentService,
								 Altinn3DefaultAttachmentService altinn3DefaultAttachmentService,
								 Altinn3ZipAttachmentService altinn3ZipAttachmentService,
								 Altinn3CorrespondenceClient altinn3CorrespondenceClient) {
		this.navDokumentService = navDokumentService;
		this.altinn3DefaultAttachmentService = altinn3DefaultAttachmentService;
		this.altinn3ZipAttachmentService = altinn3ZipAttachmentService;
		this.altinn3CorrespondenceClient = altinn3CorrespondenceClient;
		this.altinn3VedleggZipGrense = dokdistdpvProperties.getQdist016().getAltinn3VedleggZipGrense();
	}

	public UUID distribuer(HentForsendelseResponse forsendelse) {
		NavDokumenter navDokumenter = navDokumentService.hentNavDokumenter(forsendelse);

		List<UploadedAttachment> uploadedAttachments = uploadAttachments(navDokumenter);
		InitializeCorrespondencesExt initializeCorrespondencesExt = InitializeCorrespondencesMapper.map(forsendelse, uploadedAttachments);
		InitializeCorrespondencesResponseExt initializeCorrespondencesResponseExt = altinn3CorrespondenceClient.initializeCorrespondence(initializeCorrespondencesExt);
		InitializedCorrespondencesExt correspondence = initializeCorrespondencesResponseExt.getCorrespondences().getFirst();
		log.info("Sendt melding til virksomhet i Altinn3. correspondenceId={}, numAttachments={}",
				correspondence.getCorrespondenceId(),
				uploadedAttachments.size());
		return correspondence.getCorrespondenceId();
	}

	private List<UploadedAttachment> uploadAttachments(NavDokumenter navDokumenter) {
		if (navDokumenter.getVedlegg().size() > altinn3VedleggZipGrense) {
			return altinn3ZipAttachmentService.uploadAll(navDokumenter);
		} else {
			return altinn3DefaultAttachmentService.uploadAll(navDokumenter);
		}
	}
}
