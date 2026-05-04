package no.nav.dokdistdpv.consumer.rdist001;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpv.consumer.rdist001.domain.DistribuerTilNyKanalRequest;
import no.nav.dokdistdpv.consumer.rdist001.domain.FinnForsendelseRequest;
import no.nav.dokdistdpv.consumer.rdist001.domain.FinnForsendelseResponse;
import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse;
import no.nav.dokdistdpv.consumer.rdist001.domain.OppdaterForsendelseRequest;
import no.nav.dokdistdpv.exception.AdministrerForsendelseFunctionalException;
import no.nav.dokdistdpv.exception.AdministrerForsendelseTechnicalException;
import no.nav.dokdistdpv.properties.DokdistdpvProperties;
import no.nav.dokdistdpv.security.AzureToken;
import no.nav.dokdistdpv.security.WebClientAzureAuthentication;
import no.nav.dokdistdpv.utils.NavHeadersFilter;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.util.unit.DataSize.ofMegabytes;

@Slf4j
@Component
public class AdministrerForsendelseConsumer {

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
				.exchangeStrategies(ExchangeStrategies.builder()
						.codecs(configurer ->
								configurer.defaultCodecs().maxInMemorySize((int) ofMegabytes(10).toBytes()))
						.build())
				.build();
	}

	@Retryable(includes = AdministrerForsendelseTechnicalException.class)
	public HentForsendelseResponse hentForsendelse(final String forsendelseId) {

		log.info("hentForsendelse henter forsendelse med forsendelseId={}", forsendelseId);

		var response = webClient.get()
				.uri(uriBuilder -> uriBuilder
						.path("/{forsendelseId}")
						.build(forsendelseId))
				.retrieve()
				.bodyToMono(HentForsendelseResponse.class)
				.onErrorMap(this::mapError)
				.block();

		log.info("hentForsendelse har hentet forsendelse med forsendelseId={}", forsendelseId);

		return response;
	}

	@Retryable(includes = AdministrerForsendelseTechnicalException.class)
	public void oppdaterForsendelse(OppdaterForsendelseRequest oppdaterForsendelse) {
		log.info("oppdaterForsendelse oppdaterer forsendelse med forsendelseId={}", oppdaterForsendelse.forsendelseId());

		webClient.put()
				.uri("/oppdaterforsendelse")
				.bodyValue(oppdaterForsendelse)
				.retrieve()
				.toBodilessEntity()
				.onErrorMap(this::mapError)
				.block();

		oppdaterForsendelseLog(oppdaterForsendelse);
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

	@Retryable(includes = AdministrerForsendelseTechnicalException.class)
	public String finnForsendelse(final FinnForsendelseRequest finnForsendelseRequest) {
		var oppslagsnoekkel = finnForsendelseRequest.oppslagsnoekkel();
		var verdi = finnForsendelseRequest.verdi();

		log.info("finnForsendelse henter forsendelse med {}={}", oppslagsnoekkel, verdi);

		var response = webClient.get()
				.uri(uriBuilder -> uriBuilder
						.path("/finnforsendelse/{oppslagsnoekkel}/{verdi}")
						.build(oppslagsnoekkel, verdi))
				.retrieve()
				.bodyToMono(FinnForsendelseResponse.class)
				.map(FinnForsendelseResponse::forsendelseId)
				.onErrorMap(this::mapError)
				.block();

		log.info("finnForsendelse har hentet forsendelse med forsendelseId={} og {}={}", response, oppslagsnoekkel, verdi);
		return response;
	}

	@Retryable(includes = AdministrerForsendelseTechnicalException.class)
	public void distribuerTilNyKanal(final DistribuerTilNyKanalRequest distribuerTilNyKanalRequest) {

		log.info("distribuerTilNyKanal distribuerer forsendelse med forsendelseId={} til print", distribuerTilNyKanalRequest.forsendelseId());
		webClient.post()
				.uri("/distribuertilnykanal")
				.bodyValue(distribuerTilNyKanalRequest)
				.retrieve()
				.toBodilessEntity()
				.onErrorMap(this::mapError)
				.block();
	}

	private Throwable mapError(Throwable error) {
		if (error instanceof WebClientResponseException response && response.getStatusCode().is4xxClientError()) {
			return new AdministrerForsendelseFunctionalException(
					format("Kall mot AdministrerForsendelse feilet med status=%s, feilmelding=%s",
							response.getStatusCode(),
							response.getMessage()),
					error);
		} else {
			return new AdministrerForsendelseTechnicalException(
					format("Kall mot AdministrerForsendelse feilet med feilmelding=%s", error.getMessage()),
					error);
		}
	}

}
