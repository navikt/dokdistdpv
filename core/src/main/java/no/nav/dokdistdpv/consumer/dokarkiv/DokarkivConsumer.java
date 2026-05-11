package no.nav.dokdistdpv.consumer.dokarkiv;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpv.exception.DokarkivFunctionalException;
import no.nav.dokdistdpv.exception.DokarkivTechnicalException;
import no.nav.dokdistdpv.properties.DokdistdpvProperties;
import no.nav.dokdistdpv.security.AzureToken;
import no.nav.dokdistdpv.security.WebClientAzureAuthentication;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Component
public class DokarkivConsumer {

	private final String JOURNALPOST_API_URL = "/journalpostapi/v1/journalpost";
	private final WebClient webClient;

	public DokarkivConsumer(WebClient webClient,
							AzureToken azureToken,
							DokdistdpvProperties dokdistdpvProperties) {
		this.webClient = webClient.mutate()
				.baseUrl(dokdistdpvProperties.getEndpoints().getDokarkiv().getUrl())
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.filter(new WebClientAzureAuthentication(azureToken, dokdistdpvProperties.getEndpoints().getDokarkiv().getScope()))
				.build();
	}

	@Retryable(includes = DokarkivTechnicalException.class)
	public void oppdaterDistribusjonsinfo(String journalpostId, OppdaterDistribusjonsinfoRequest oppdaterDistribusjonsinfoRequest) {
		webClient.patch()
				.uri(uriBuilder -> uriBuilder
						.path(JOURNALPOST_API_URL + "/{journalpostId}/oppdaterDistribusjonsinfo")
						.build(journalpostId))
				.bodyValue(oppdaterDistribusjonsinfoRequest)
				.retrieve()
				.toBodilessEntity()
				.onErrorMap(this::mapError)
				.block();
	}

	private Throwable mapError(Throwable error) {
		if (error instanceof WebClientResponseException response && response.getStatusCode().is4xxClientError()) {
			return new DokarkivFunctionalException(
					format("Kall mot oppdaterDistribusjonsinfo feilet med status=%s, feilmelding=%s",
							response.getStatusCode(),
							response.getMessage()),
					error);
		} else {
			return new DokarkivTechnicalException(
					format("Kall mot oppdaterDistribusjonsinfo feilet med feilmelding=%s", error.getMessage()),
					error);
		}
	}
}
