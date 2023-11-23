package no.nav.dokdistdpv.consumer.dokarkiv;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpv.exception.DokarkivFunctionalException;
import no.nav.dokdistdpv.exception.DokarkivTechnicalException;
import no.nav.dokdistdpv.properties.DokdistdpvProperties;
import no.nav.dokdistdpv.security.AzureToken;
import no.nav.dokdistdpv.security.WebClientAzureAuthentication;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static no.nav.dokdistdpv.consumer.rdist001.domain.DistribusjonKanal.DPVT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Component
public class DokarkivConsumer {

	private static final int FRA_ANTALL_DAGER_TILBAKE = 6;
	private static final LocalDateTime EKSPEDERT_FRA = now().minusDays(FRA_ANTALL_DAGER_TILBAKE);
	private static final int TIL_ANTALL_DAGER = 3;
	private static final LocalDateTime EKSPEDERT_TIL = EKSPEDERT_FRA.plusDays(TIL_ANTALL_DAGER);

	private static final String FINN_ULESTJOURNALPOST_API_PATH = "/internal/sikkerhetsnivaa/finnUlesteJournalposter/";
	private final String JOURNALPOST_API_URL = "/journalpostapi/v1/journalpost";
	private final WebClient webClient;

	public DokarkivConsumer(WebClient webClient, AzureToken azureToken, DokdistdpvProperties dokdistdpvProperties) {
		this.webClient = webClient.mutate()
				.baseUrl(dokdistdpvProperties.getEndpoints().getDokarkiv().getUrl())
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.filter(new WebClientAzureAuthentication(azureToken, dokdistdpvProperties.getEndpoints().getDokarkiv().getScope()))
				.build();
	}

	@Retryable(retryFor = DokarkivTechnicalException.class)
	public List<String> finnUlestJournalposter() {
		log.info(String.format("finnUlesteJournalposter har mottatt kall for å finne journalposter fra kanal=%s med ekspedertFra=%s og ekspedertTil=%s.",
				DPVT, EKSPEDERT_FRA, EKSPEDERT_TIL));

		String[] journalposter = webClient.get()
				.uri(uriBuilder -> uriBuilder.path(FINN_ULESTJOURNALPOST_API_PATH + DPVT + "/" + EKSPEDERT_FRA + "/" + EKSPEDERT_TIL)
						.build())
				.retrieve()
				.bodyToMono(String[].class)
				.doOnError(this::handleError)
				.block();

		if (journalposter != null && journalposter.length > 0) {
			return Arrays.stream(journalposter).toList();
		}
		return Collections.emptyList();
	}

	@Retryable(retryFor = DokarkivTechnicalException.class)
	public void oppdaterDistribusjonsinfo(String journalpostId, OppdaterDistribusjonsinfoRequest oppdaterDistribusjonsinfoRequest) {
		log.info("oppdaterDistribusjonsinfo mottatt kall til å oppdatere datoLest for journalpostId={}", journalpostId);

		webClient.patch()
				.uri(uriBuilder -> uriBuilder
						.path(JOURNALPOST_API_URL + "/{journalpostId}/oppdaterDistribusjonsinfo")
						.build(journalpostId))
				.bodyValue(oppdaterDistribusjonsinfoRequest)
				.retrieve()
				.bodyToMono(String.class)
				.doOnError(this::handleError)
				.block();
	}

	private void handleError(Throwable error) {
		if (error instanceof WebClientResponseException response && ((WebClientResponseException) error).getStatusCode().is4xxClientError()) {
			throw new DokarkivFunctionalException(
					format("Kall mot Journalpost-API feilet med status=%s, feilmelding=%s", response.getStatusCode(), response.getMessage()),
					error) {
			};
		} else {
			throw new DokarkivTechnicalException(
					format("Kall mot Journalpost-API feilet med feilmelding=%s", error.getMessage()),
					error);
		}
	}
}
