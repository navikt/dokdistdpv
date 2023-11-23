package no.nav.dokdistdpv.consumer.rdist001;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpv.consumer.rdist001.domain.DistribusjonsTypeKode;
import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse;
import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelserResponse;
import no.nav.dokdistdpv.consumer.rdist001.domain.OppdaterForsendelseRequest;
import no.nav.dokdistdpv.exception.AdministrerForsendelseFunctionalException;
import no.nav.dokdistdpv.exception.AdministrerForsendelseTechnicalException;
import no.nav.dokdistdpv.properties.DokdistdpvProperties;
import no.nav.dokdistdpv.security.AzureToken;
import no.nav.dokdistdpv.security.WebClientAzureAuthentication;
import no.nav.dokdistdpv.utils.NavHeadersFilter;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;

import static java.lang.String.format;
import static no.nav.dokdistdpv.consumer.rdist001.domain.DistribusjonKanal.DPVT;
import static no.nav.dokdistdpv.consumer.rdist001.domain.DistribusjonsTypeKode.VEDTAK;
import static no.nav.dokdistdpv.consumer.rdist001.domain.DistribusjonsTypeKode.VIKTIG;
import static no.nav.dokdistdpv.consumer.rdist001.domain.DokumentStatus.EKSPEDERT;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Component
public class AdministrerForsendelseConsumer {

	private static final List<DistribusjonsTypeKode> DISTRIBUSJONS_TYPER = List.of(VEDTAK, VIKTIG);
	private final WebClient webClient;

	public AdministrerForsendelseConsumer(WebClient webClient,
										  DokdistdpvProperties dokdistdpvProperties,
										  AzureToken azureToken) {
		this.webClient = webClient.mutate()
				.baseUrl(dokdistdpvProperties.getEndpoints().getDokdistadmin().getUrl())
				.filter(new WebClientAzureAuthentication(azureToken,
						dokdistdpvProperties.getEndpoints().getDokdistadmin().getScope()))
				.filter(new NavHeadersFilter())
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.build();
	}

	@Retryable(retryFor = AdministrerForsendelseTechnicalException.class)
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

	@Retryable(retryFor = AdministrerForsendelseTechnicalException.class)
	public void oppdaterForsendelse(OppdaterForsendelseRequest oppdaterForsendelse) {
		log.info("oppdaterForsendelse oppdaterer forsendelse med forsendelseId={}", oppdaterForsendelse.forsendelseId());

		webClient.put()
				.uri("/oppdaterforsendelse")
				.bodyValue(oppdaterForsendelse)
				.retrieve()
				.toBodilessEntity()
				.doOnError(this::handleError)
				.block();

		oppdaterForsendelseLog(oppdaterForsendelse);
	}

	@Retryable(retryFor = AdministrerForsendelseTechnicalException.class)
	public HentForsendelserResponse hentForsendelser(List<String> journalpostliste) {
		return webClient.get()
				.uri(uriBuilder -> uriBuilder.path("/hentForsendelser")
						.queryParam("distribusjonstyper", DISTRIBUSJONS_TYPER)
						.queryParam("dokumentstatus", EKSPEDERT)
						.queryParam("distribusjonkanal", DPVT)
						.queryParam("inkluderAvstemte", false)
						.queryParam("journalpostliste", journalpostliste)
						.build())
				.retrieve()
				.bodyToMono(HentForsendelserResponse.class)
				.onErrorResume(Throwable.class, err -> {
					log.warn("hentForsendelser feilet med feilmelding={}", err.getMessage());
					return Mono.empty();
				})
				.block();
	}

	@Retryable(retryFor = AdministrerForsendelseTechnicalException.class)
	public void oppdaterForsendelserAvstemtDatoOgReferanse(OppdaterForsendelserAvstemtInfo oppdaterForsendelserAvstemtInfo) {
		log.info("oppdaterForsendelserAvstemDatoOgReferanse har mottatt kall om å oppdatere {} forsendelser fra rdist001 med avstemtReferanse={}",
				oppdaterForsendelserAvstemtInfo.getForsendelser().size(), oppdaterForsendelserAvstemtInfo.getAvstemtReferanse());
		webClient.put()
				.uri("/avstemforsendelser")
				.bodyValue(oppdaterForsendelserAvstemtInfo)
				.retrieve()
				.toBodilessEntity()
				.doOnSuccess(response ->
						log.info("avstemforsendelser har oppdatert {} forsendelser med avstemtReferanse og avstemtDato", oppdaterForsendelserAvstemtInfo.getForsendelser().size()))
				.doOnError(Throwable.class, err ->
						log.warn("oppdaterForsendelserAvstemtDatoOgReferanse feilet med feilmelding={}", err.getMessage()))
				.block();
	}

	private void oppdaterForsendelseLog(OppdaterForsendelseRequest oppdaterForsendelse) {
		if (isNotBlank(oppdaterForsendelse.forsendelseStatus())) {
			log.info("oppdaterForsendelse har oppdatert forsendelse med forsendelseId={} til forsendelseStatus={}",
					oppdaterForsendelse.forsendelseId(), oppdaterForsendelse.forsendelseStatus());
		}
		if (isNotBlank(oppdaterForsendelse.konversasjonId())) {
			log.info("oppdaterForsendelse har oppdatert forsendelse med forsendelseId={} til konversasjonId={}",
					oppdaterForsendelse.forsendelseId(), oppdaterForsendelse.konversasjonId());
		}
	}

	private void handleError(Throwable error) {
		if (error instanceof WebClientResponseException response && ((WebClientResponseException) error).getStatusCode().is4xxClientError()) {
			throw new AdministrerForsendelseFunctionalException(
					format("Kall mot AdministrerForsendelse feilet med status=%s, feilmelding=%s",
							response.getStatusCode(),
							response.getMessage()),
					error);
		} else {
			throw new AdministrerForsendelseTechnicalException(
					format("Kall mot AdministrerForsendelse feilet med feilmelding=%s", error.getMessage()),
					error);
		}
	}
}
