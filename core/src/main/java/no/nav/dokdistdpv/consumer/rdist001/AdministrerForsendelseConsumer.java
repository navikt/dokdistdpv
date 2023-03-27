package no.nav.dokdistdpv.consumer.rdist001;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse;
import no.nav.dokdistdpv.exception.AdministrerForsendelseFunctionalException;
import no.nav.dokdistdpv.exception.AdministrerForsendelseTechnicalException;
import no.nav.dokdistdpv.properties.DokdistdpvProperties;
import no.nav.dokdistdpv.security.AzureToken;
import no.nav.dokdistdpv.security.WebClientAzureAuthentication;
import no.nav.dokdistdpv.utils.MDCOperations;
import no.nav.dokdistdpv.utils.NavHeadersFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Component
public class AdministrerForsendelseConsumer {

	private final WebClient administrerForsendelseClient;
	private final WebClient webClient;

	public AdministrerForsendelseConsumer(WebClient administrerForsendelseClient,
										  WebClient webClient,
										  DokdistdpvProperties dokdistdpvProperties,
										  AzureToken azureToken) {
		this.administrerForsendelseClient = administrerForsendelseClient;
		this.webClient = webClient.mutate()
				.baseUrl(dokdistdpvProperties.getEndpoints().getDokdistadmin().getUrl())
				.filter(new WebClientAzureAuthentication(azureToken,
						dokdistdpvProperties.getEndpoints().getDokdistadmin().getScope()))
				.filter(new NavHeadersFilter())
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.build();
	}

	public HentForsendelseResponse hentForsendelse(final String forsendelseId) {

		log.info("hentForsendelse henter forsendelse med forsendelseId={}", forsendelseId);

		var response = webClient.get()
				.uri(uriBuilder -> uriBuilder
						.path("/{forsendelseId}")
						.build(forsendelseId))
				.retrieve()
				.bodyToMono(HentForsendelseResponse.class)
				.doOnError(this::handleError)
				.block();

		log.info("hentForsendelse har hentet forsendelse med forsendelseId={}", forsendelseId);

		return response;
	}

	public void persisterKonversasjonId(String forsendelseId, String konversasjonId) {

		administrerForsendelseClient.put()
				.uri(uriBuilder -> uriBuilder
						.path("/rest/v1/administrerforsendelse/")
						.queryParam("forsendelseId", forsendelseId)
						.queryParam("konversasjonId", konversasjonId)
						.build())
				.header("Nav-Call-Id", MDCOperations.getCallId())
				.retrieve()
				.toBodilessEntity()
				.doOnError(this::handleError)
				.block();
	}

	public void oppdaterStatus(String forsendelseId, String forsendelseStatus) {

		administrerForsendelseClient.put()
				.uri(uriBuilder -> uriBuilder
						.path("/rest/v1/administrerforsendelse/")
						.queryParam("forsendelseId", forsendelseId)
						.queryParam("forsendelseStatus", forsendelseStatus)
						.build())
				.header("Nav-Call-Id", MDCOperations.getCallId())
				.retrieve()
				.toBodilessEntity()
				.doOnError(this::handleError)
				.block();
	}

	private void handleError(Throwable error) {
		if(error instanceof WebClientResponseException response && ((WebClientResponseException) error).getStatusCode().is4xxClientError()) {
			throw new AdministrerForsendelseFunctionalException(
					format("Kall mot AdministrerForsendelse feilet med status=%s, feilmelding=%s",
							response.getRawStatusCode(),
							response.getMessage()),
					error);
		} else {
			throw new AdministrerForsendelseTechnicalException(
					format("Kall mot AdministrerForsendelse feilet med feilmelding=%s", error.getMessage()),
					error);
		}
	}
}
