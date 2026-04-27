package no.nav.dokdistdpv.consumer.altinn3;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.altinn.services.altinn3.openapi.domain.AttachmentOverviewExt;
import no.altinn.services.altinn3.openapi.domain.InitializeAttachmentExt;
import no.altinn.services.altinn3.openapi.domain.InitializeCorrespondencesExt;
import no.altinn.services.altinn3.openapi.domain.InitializeCorrespondencesResponseExt;
import no.altinn.services.altinn3.openapi.domain.ProblemDetails;
import no.nav.dokdistdpv.consumer.altinn3.api.correspondence.exceptions.AttachmentIsNotPublishedException;
import no.nav.dokdistdpv.exception.AltinnException;
import no.nav.dokdistdpv.exception.DokdistdpvTechnicalException;
import no.nav.dokdistdpv.properties.DokdistdpvProperties;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;

import static no.nav.dokdistdpv.consumer.altinn3.api.correspondence.CorrespondenceErrorCodes.ATTACHMENT_IS_NOT_PUBLISHED;
import static no.nav.dokdistdpv.consumer.token.NaisTexasRequestInterceptor.MASKINPORTEN_TARGET_SCOPES;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

/// Klientimplementasjon av [Altinn.Correspondence.API](https://docs.altinn.studio/nb/api/correspondence/spec/#/)
@Slf4j
@Component
public class Altinn3CorrespondenceClient {
	private final RestClient restClientTexas;
	private final ObjectMapper objectMapper;
	private final String altinn3CorrespondenceWriteScopes;

	public Altinn3CorrespondenceClient(RestClient restClientTexas,
									   DokdistdpvProperties dokdistdpvProperties,
									   ObjectMapper objectMapper) {
		this.restClientTexas = restClientTexas.mutate()
				.baseUrl(dokdistdpvProperties.getEndpoints().getAltinn3().getUrl())
				.build();

		this.altinn3CorrespondenceWriteScopes = dokdistdpvProperties.getEndpoints().getAltinn3().getScope();
		this.objectMapper = objectMapper;
	}

	/// [Initialize Attachment](https://docs.altinn.studio/nb/api/correspondence/spec/#/Attachment/post_correspondence_api_v1_attachment)
	///
	/// Initialize a new Attachment to be shared in correspondences
	///
	/// @return Attachment Id
	@Retryable(retryFor = DokdistdpvTechnicalException.class)
	public String initializeAttachment(InitializeAttachmentExt initializeAttachmentExt) {
		String json = restClientTexas.post()
				.uri("/correspondence/api/v1/attachment")
				.attribute(MASKINPORTEN_TARGET_SCOPES, altinn3CorrespondenceWriteScopes)
				.accept(APPLICATION_JSON)
				.contentType(APPLICATION_JSON)
				.body(initializeAttachmentExt)
				.retrieve()
				.onStatus(HttpStatusCode::isError, (_, response) -> {
					String feilmelding = "attachment feilet med problemdetail=%s";
					feilhandtering(response, feilmelding);
				})
				.body(String.class);
		try {
			return objectMapper.readValue(json, String.class);
		} catch (JsonProcessingException _) {
			throw new DokdistdpvTechnicalException("Klarte ikke å deserialisere json");
		}
	}

	/// [Upload Attachment](https://docs.altinn.studio/nb/api/correspondence/spec/#/Attachment/post_correspondence_api_v1_attachment__attachmentId__upload)
	///
	/// Initialize Correspondences and uploads attachments in the same request
	@Retryable(retryFor = DokdistdpvTechnicalException.class)
	public AttachmentOverviewExt uploadAttachment(String attachmentId, byte[] attachment) {
		return restClientTexas.post()
				.uri("/correspondence/api/v1/attachment/{attachmentId}/upload", attachmentId)
				.attribute(MASKINPORTEN_TARGET_SCOPES, altinn3CorrespondenceWriteScopes)
				.accept(APPLICATION_JSON)
				.contentType(APPLICATION_OCTET_STREAM)
				.body(attachment)
				.retrieve()
				.onStatus(HttpStatusCode::isError, (_, response) -> {
					String feilmelding = "attachment/upload feilet med problemdetail=%s";
					feilhandtering(response, feilmelding);
				})
				.body(AttachmentOverviewExt.class);
	}

	/// [Upload Attachment](https://docs.altinn.studio/nb/api/correspondence/spec/#/Attachment/post_correspondence_api_v1_attachment__attachmentId__upload)
	///
	/// Initialize Correspondences
	@Retryable(retryFor = {DokdistdpvTechnicalException.class, AttachmentIsNotPublishedException.class}, maxAttempts = 6,
			backoff = @Backoff(delay = 2000, multiplier = 3, maxDelay = 60000))
	public InitializeCorrespondencesResponseExt initializeCorrespondence(InitializeCorrespondencesExt initializeCorrespondencesExt) {
		return restClientTexas.post()
				.uri("/correspondence/api/v1/correspondence")
				.attribute(MASKINPORTEN_TARGET_SCOPES, altinn3CorrespondenceWriteScopes)
				.accept(APPLICATION_JSON)
				.contentType(APPLICATION_JSON)
				.body(initializeCorrespondencesExt)
				.retrieve()
				.onStatus(HttpStatusCode::isError, (_, response) -> {
					String feilmelding = "correspondence feilet med problemdetail=%s";
					feilhandtering(response, feilmelding);
				})
				.body(InitializeCorrespondencesResponseExt.class);
	}

	private void feilhandtering(ClientHttpResponse response, String feilmelding) throws IOException {
		ProblemDetails problemDetail = mapProblemDetail(response);

		if (response.getStatusCode().is4xxClientError()) {
			int errorCode = mapErrorCode(problemDetail);
			if (errorCode == ATTACHMENT_IS_NOT_PUBLISHED) {
				log.info("initializeCorrespondence gir errorCode={}, forsøker retry", ATTACHMENT_IS_NOT_PUBLISHED);
				throw new AttachmentIsNotPublishedException(feilmelding.formatted(problemDetail));
			}
			throw new AltinnException(feilmelding.formatted(problemDetail));
		} else {
			throw new DokdistdpvTechnicalException(feilmelding.formatted(problemDetail));
		}
	}

	private static int mapErrorCode(ProblemDetails problemDetail) {
		Object errorCode = problemDetail.getAdditionalProperty("errorCode");
		return errorCode == null ? 0 : (int) errorCode;
	}

	private ProblemDetails mapProblemDetail(ClientHttpResponse response) throws IOException {
		byte[] body = response.getBody().readAllBytes();
		try {
			return objectMapper.readValue(body, ProblemDetails.class);
		} catch (JsonMappingException _) {
			return ProblemDetails.builder()
					.status(response.getStatusCode().value())
					.detail(new String(body))
					.build();
		}
	}
}
