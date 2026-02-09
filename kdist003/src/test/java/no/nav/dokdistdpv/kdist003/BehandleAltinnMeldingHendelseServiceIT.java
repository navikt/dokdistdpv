package no.nav.dokdistdpv.kdist003;

import no.nav.dokdistdpv.kdist003.config.AbstractIT;
import no.nav.dokdistdpv.kdist003.domain.AltinnEventMelding;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static java.time.Duration.ofSeconds;
import static no.nav.dokdistdpv.kdist003.Kdist003Validator.ALTINN_EVENT_TYPE_OPPDATER_TIL_EKSPEDERT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


class BehandleAltinnMeldingHendelseServiceIT extends AbstractIT {

	@Autowired
	private BehandleAltinnMeldingHendelseService behandleAltinnMeldingHendelseService;

	@BeforeEach
	void setUp() {
		stubAzure();
	}

	@Test
	void shouldLesMeldingFraTopicAndUpdateForsendelseStatus() {
		stubFinnForsendelse();
		stubHentForsendelse();

		sendToInnTopic(createMelding(ALTINN_EVENT_TYPE_OPPDATER_TIL_EKSPEDERT));

		behandleAltinnMeldingHendelseService.behandleAltinnMelding(getConsumerRecord());
	}

	ConsumerRecord<String, AltinnEventMelding> getConsumerRecord() {
		return KafkaTestUtils.getSingleRecord(consumer, PRIVAT_ALTINN_MELDING_TOPIC, ofSeconds(20));
	}

	void stubFinnForsendelse() {
		stubFor(get(urlPathMatching(FINN_FORSENDELSE_PATH))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("dokdistadmin/finn-forsendelse.json")));
	}

	void stubHentForsendelse() {
		stubFor(get(urlMatching(HENT_FORSENDELSE_PATH + "/1720847"))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("/dokdistadmin/hent-forsendelse.json")
				));
	}

	AltinnEventMelding createMelding(String type) {
		return AltinnEventMelding.builder()
				.id(UUID.randomUUID())
				.resourceinstance(UUID.fromString("af0e7e0c-579c-4563-9398-10cdf031b80d"))
				.resource("urn:altinn:resource:nav_dokumentdistribusjon_taushetsbelagtpost")
				.source(URI.create("https://ttd.apps.altinn.no/ttd/apps-test/instances/50015641/a72223a3-926b-4095-a2a6-bacc10815f2d"))
				.type(type)
				.alternativesubject("/organisation/889640782")
				.specversion("1.0")
				.time(OffsetDateTime.now())
				.build();
	}

	private void stubAzure() {
		stubFor(post("/azure_token")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("azure/token_response.json")));
	}

}