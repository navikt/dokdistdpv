package no.nav.dokdistdpv.consumer.dokarkiv;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpv.exception.DokarkivFunctionalException;
import no.nav.dokdistdpv.exception.DokarkivTechnicalException;
import no.nav.dokdistdpv.properties.DokdistdpvProperties;
import no.nav.dokdistdpv.security.AzureToken;
import no.nav.dokdistdpv.security.WebClientAzureAuthentication;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
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

	private static final int ANTALL_DAGER_TILBAKE = 6;
	private static final LocalDateTime EKSPEDERT_FRA = now().minusDays(ANTALL_DAGER_TILBAKE);
	private static final int ANTALL_DAGER_FREM = 3;
	private static final LocalDateTime EKSPEDERT_TIL = EKSPEDERT_FRA.plusDays(ANTALL_DAGER_FREM);

	private static final String FINN_ULESTE_JOURNALPOST_PATH = "/internal/sikkerhetsnivaa/finnUlesteJournalposter/";
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
	public List<String> finnUlesteJournalposter() {
		log.info(format("finnUlesteJournalposter har mottatt kall for å finne journalposter fra kanal=%s med ekspedertFra=%s og ekspedertTil=%s.",
				DPVT, EKSPEDERT_FRA, EKSPEDERT_TIL));

		List<String> journalposter = webClient.get()
				.uri(uriBuilder -> uriBuilder.path(FINN_ULESTE_JOURNALPOST_PATH + DPVT + "/" + EKSPEDERT_FRA + "/" + EKSPEDERT_TIL)
						.build())
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<List<String>>() {})
				.onErrorResume(Throwable.class, err -> {
					log.warn("finnUlesteJournalposter feilet med feilmelding={}", err.getMessage());
					return Mono.empty();
				})
				.block();

		return journalposter.isEmpty() ? Collections.emptyList() : journalposter;
	}

	@Retryable(retryFor = DokarkivTechnicalException.class)
	public void oppdaterDistribusjonsinfo(String journalpostId, OppdaterDistribusjonsinfoRequest oppdaterDistribusjonsinfoRequest) {
		log.info("oppdaterDistribusjonsinfo har mottatt kall om å oppdatere datoLest for journalpostId={}", journalpostId);

		webClient.patch()
				.uri(uriBuilder -> uriBuilder
						.path(JOURNALPOST_API_URL + "/{journalpostId}/oppdaterDistribusjonsinfo")
						.build(journalpostId))
				.bodyValue(oppdaterDistribusjonsinfoRequest)
				.retrieve()
				.toBodilessEntity()
				.doOnError(Throwable.class, err -> log.warn("Kall mot Journalpost-API feilet med feilmelding={}", err.getMessage()))
				.block();
	}
}
