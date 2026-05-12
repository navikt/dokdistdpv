package no.nav.dokdistdpv.kdist003;

import no.altinn.event.domain.CloudEvent;
import no.nav.dokdistdpv.kdist003.config.AbstractIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.patchRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.util.concurrent.TimeUnit.SECONDS;
import static no.nav.dokdistdpv.consumer.rdist001.domain.DistribuerTilNyKanalRequest.ARSAK_PUBLISERING_FEILET;
import static no.nav.dokdistdpv.consumer.rdist001.domain.DistribuerTilNyKanalRequest.ARSAK_VARSLING_FEILET;
import static no.nav.dokdistdpv.consumer.rdist001.domain.DistribuerTilNyKanalRequest.MELDINGSFEIL;
import static no.nav.dokdistdpv.consumer.rdist001.domain.DistribuerTilNyKanalRequest.VARSLINGSFEIL;
import static no.nav.dokdistdpv.kdist003.BehandleAltinnMeldingHendelseService.FORSENDELSE_STATUS_EKSPEDERT;
import static no.nav.dokdistdpv.kdist003.Kdist003Constants.MELDING_FEILET_HENDELSESTYPE;
import static no.nav.dokdistdpv.kdist003.Kdist003Constants.OPPDATER_LEST_DATO_HENDELSESTYPER;
import static no.nav.dokdistdpv.kdist003.Kdist003Constants.OPPDATER_TIL_EKSPEDERT_HENDELSESTYPER;
import static no.nav.dokdistdpv.kdist003.Kdist003Constants.OPPRETTELSE_VARSLING_FEILET_HENDELSESTYPE;
import static no.nav.dokdistdpv.kdist003.Kdist003Constants.VARSLING_FEILET_HENDELSESTYPE;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


class BehandleAltinnMeldingHendelseServiceIT extends AbstractIT {

	private static final String FORSENDELSE_ID = "1720847";

	@BeforeEach
	void setUp() {
		stubAzure();
	}

	@ParameterizedTest
	@MethodSource
	void shouldReadEventTypePublishedAndUpdateForsendelseStatusToEkspedert(String type) {
		stubFinnForsendelse();
		stubHentForsendelse();
		stubPutOppdaterForsendelse();

		sendToInnTopic(createMelding(type));

		await().atMost(10, SECONDS).untilAsserted(() -> {
			try {
				verify(putRequestedFor(urlEqualTo(OPPDATER_FORSENDELSE_PATH))
						.withRequestBody(matchingJsonPath("$.forsendelseId", equalTo(FORSENDELSE_ID)))
						.withRequestBody(matchingJsonPath("$.forsendelseStatus", equalTo(FORSENDELSE_STATUS_EKSPEDERT))));
			} catch (RuntimeException _) {
				fail();
			}
		});
	}

	static Stream<Arguments> shouldReadEventTypePublishedAndUpdateForsendelseStatusToEkspedert() {
		return OPPDATER_TIL_EKSPEDERT_HENDELSESTYPER.stream().map(Arguments::of);
	}

	@ParameterizedTest
	@MethodSource
	void shouldReadEventsConfirmedOrReadAndUpdateJournalpostLestDato(String type) {
		stubFinnForsendelse();
		stubHentForsendelse("hent_forsendelse_ekspedert_response.json");
		stubPutOppdaterForsendelse();
		stubPatchOppdaterDistribusjonsinfo();

		sendToInnTopic(createMelding(type));

		await().atMost(10, SECONDS).untilAsserted(() -> {
			try {
				verify(patchRequestedFor(urlMatching(OPPDATERDISTRIBUSJONSINFO_URL))
						.withRequestBody(matchingJsonPath("$.datoLest", equalTo("2026-04-16T09:42:29Z"))));
			} catch (RuntimeException _) {
				fail();
			}
		});
	}

	static Stream<Arguments> shouldReadEventsConfirmedOrReadAndUpdateJournalpostLestDato() {
		return OPPDATER_LEST_DATO_HENDELSESTYPER.stream().map(Arguments::of);
	}

	@ParameterizedTest
	@MethodSource
	void shouldReadEventTypePublishFailedOrNotificationCreationFailedAndSendMeldingToPrint(String type, String expectedArsak, String expectedArsakbeskrivelse) {
		stubFinnForsendelse();
		stubHentForsendelse();
		stubPostDistribuerTilPrint();
		stubPatchOppdaterDistribusjonsinfo();

		sendToInnTopic(createMelding(type));

		await().atMost(10, SECONDS).untilAsserted(() -> {
			try {
				verify(patchRequestedFor(urlMatching(OPPDATERDISTRIBUSJONSINFO_URL))
						.withRequestBody(matchingJsonPath("$.tilbakestillJournalpost", equalTo("true"))));
				verify(postRequestedFor(urlEqualTo(DISTRIBUERTILPRINT_PATH))
						.withRequestBody(matchingJsonPath("$.forsendelseId", equalTo(FORSENDELSE_ID)))
						.withRequestBody(matchingJsonPath("$.kanal", equalTo("PRINT")))
						.withRequestBody(matchingJsonPath("$.arsak", equalTo(expectedArsak)))
						.withRequestBody(matchingJsonPath("$.arsakBeskrivelse", equalTo(expectedArsakbeskrivelse))));
			} catch (RuntimeException _) {
				fail();
			}
		});
	}

	static Stream<Arguments> shouldReadEventTypePublishFailedOrNotificationCreationFailedAndSendMeldingToPrint() {
		return Stream.of(
				Arguments.of(MELDING_FEILET_HENDELSESTYPE, MELDINGSFEIL, ARSAK_PUBLISERING_FEILET),
				Arguments.of(VARSLING_FEILET_HENDELSESTYPE, VARSLINGSFEIL, ARSAK_VARSLING_FEILET),
				Arguments.of(OPPRETTELSE_VARSLING_FEILET_HENDELSESTYPE, VARSLINGSFEIL, ARSAK_VARSLING_FEILET)
		);
	}

	void stubFinnForsendelse() {
		stubFor(get(urlEqualTo(FINN_FORSENDELSE_PATH))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("dokdistadmin/finnforsendelse_happy.json")));
	}

	void stubHentForsendelse() {
		stubHentForsendelse("hent_forsendelse_response.json");
	}

	void stubHentForsendelse(String fil) {
		stubFor(get(urlEqualTo(HENT_FORSENDELSE_PATH + "/" + FORSENDELSE_ID))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("dokdistadmin/" + fil)
				));
	}

	CloudEvent createMelding(String type) {
		return CloudEvent.builder()
				.id(UUID.randomUUID())
				.resourceinstance(UUID.fromString("af0e7e0c-579c-4563-9398-10cdf031b80d"))
				.resource("urn:altinn:resource:nav_dokumentdistribusjon_taushetsbelagtpost")
				.source(URI.create("https://ttd.apps.altinn.no/ttd/apps-test/instances/50015641/a72223a3-926b-4095-a2a6-bacc10815f2d"))
				.type(type)
				.alternativesubject("/organisation/889640782")
				.specversion("1.0")
				.time(OffsetDateTime.parse("2026-04-16T09:42:29Z"))
				.build();
	}

	private void stubAzure() {
		stubFor(post("/azure_token")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("azure/token_response.json")));
	}

	private void stubPutOppdaterForsendelse() {
		stubFor(put(OPPDATER_FORSENDELSE_PATH)
				.willReturn(aResponse()
						.withStatus(OK.value())));
	}

	private void stubPatchOppdaterDistribusjonsinfo() {
		stubFor(patch(urlPathMatching(OPPDATERDISTRIBUSJONSINFO_URL))
				.willReturn(aResponse()
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withStatus(OK.value())));
	}

	private void stubPostDistribuerTilPrint() {
		stubFor(post(DISTRIBUERTILPRINT_PATH)
				.willReturn(aResponse()
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withStatus(OK.value())));
	}

}