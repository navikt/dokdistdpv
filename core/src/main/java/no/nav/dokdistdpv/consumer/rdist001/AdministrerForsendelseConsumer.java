package no.nav.dokdistdpv.consumer.rdist001;

import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse;
import no.nav.dokdistdpv.exception.AdministrerForsendelseFunctionalException;
import no.nav.dokdistdpv.exception.AdministrerForsendelseTechnicalException;
import no.nav.dokdistdpv.utils.MDCOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static java.lang.String.format;

@Component
public class AdministrerForsendelseConsumer {

	private final WebClient administrerForsendelseClient;

	public AdministrerForsendelseConsumer(WebClient administrerForsendelseClient) {
		this.administrerForsendelseClient = administrerForsendelseClient;
	}

	public HentForsendelseResponse hentForsendelse(final String forsendelseId) {

		return administrerForsendelseClient.get()
				.uri("/rest/v1/administrerforsendelse/" + forsendelseId)
				.header("Nav-Call-Id", MDCOperations.getCallId())
				.retrieve()
				.bodyToMono(HentForsendelseResponse.class)
				.doOnError(this::handleError)
				.block();
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
