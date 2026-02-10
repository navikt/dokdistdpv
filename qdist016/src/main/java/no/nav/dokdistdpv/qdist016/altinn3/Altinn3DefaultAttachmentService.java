package no.nav.dokdistdpv.qdist016.altinn3;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpv.cloudstorage.EncryptedBucketStorage;
import no.nav.dokdistdpv.consumer.altinn3.Altinn3CorrespondenceClient;
import no.nav.dokdistdpv.exception.DokdistdpvTechnicalException;
import no.nav.dokdistdpv.qdist016.dokument.NavDokumenter;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.StructuredTaskScope;

@Slf4j
@Component
public class Altinn3DefaultAttachmentService extends AbstractAltinn3AttachmentService {

	Altinn3DefaultAttachmentService(Altinn3CorrespondenceClient altinn3CorrespondenceClient,
									EncryptedBucketStorage storage) {
		super(altinn3CorrespondenceClient, storage);
	}

	public List<UploadedAttachment> uploadAll(NavDokumenter navDokumenter) {
		try (var scope = StructuredTaskScope.open(StructuredTaskScope.Joiner.<UploadedAttachment>allSuccessfulOrThrow(),
				configuration -> configuration.withTimeout(Duration.ofMinutes(4)))) {
			navDokumenter.hoveddokumentOgVedlegg().forEach(navDokument ->
					scope.fork(() -> fetchInitializeUpload(navDokumenter.getBestillingsId(), navDokument)));
			try {
				return scope.join().map(StructuredTaskScope.Subtask::get).toList();
			} catch (InterruptedException e) {
				throw new DokdistdpvTechnicalException("Opplasting av Nav dokumenter til Altinn avbrutt", e);
			}
		}
	}
}
