package no.nav.dokdistdpv.consumer.rdist001;

import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse;
import no.nav.dokdistdpv.exception.AdministrerForsendelseFunctionalException;
import no.nav.dokdistdpv.exception.AdministrerForsendelseTechnicalException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class AdministrerForsendelseConsumer {

	private final WebClient administrerForsendelseClient;

	public AdministrerForsendelseConsumer(WebClient administrerForsendelseClient) {
		this.administrerForsendelseClient = administrerForsendelseClient;
	}

	public HentForsendelseResponse hentForsendelse(final String forsendelseId) {

		return administrerForsendelseClient.get()
				.uri("/rest/v1/administrerforsendelse/" + forsendelseId)
				.header("Nav-Call-Id", "ett eller annet") //TODO
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
				.retrieve()
				.toBodilessEntity()
				.doOnError(this::handleError)
				.block();
	}

	private void handleError(Throwable error) {
		if(error instanceof WebClientResponseException response && ((WebClientResponseException) error).getStatusCode().is4xxClientError()) {
			throw new AdministrerForsendelseFunctionalException(
					String.format("Kall mot AdministrerForsendelse feilet med status=%s, feilmelding=%s",
							response.getRawStatusCode(),
							response.getMessage()),
					error);
		} else {
			throw new AdministrerForsendelseTechnicalException(
					String.format("Kall mot AdministrerForsendelse feilet med feilmelding=%s", error.getMessage()),
					error);
		}
	}
}
