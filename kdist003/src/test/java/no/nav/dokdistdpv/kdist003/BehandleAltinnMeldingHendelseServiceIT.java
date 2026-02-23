package no.nav.dokdistdpv.kdist003;

import no.nav.dokdistdpv.kdist003.config.AbstractIT;
import no.nav.dokdistdpv.kdist003.domain.AltinnEventMelding;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.reset;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static no.nav.dokdistdpv.kdist003.Kdist003Constants.ALTINN_EVENT_TYPE_OPPDATER_TIL_EKSPEDERT;
import static no.nav.dokdistdpv.kdist003.Kdist003Constants.CORRESPONDENCE_NOTIFICATION_CREATION_FAILED;
import static no.nav.dokdistdpv.kdist003.Kdist003Constants.CORRESPONDENCE_PUBLISH_FAILED;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


class BehandleAltinnMeldingHendelseServiceIT extends AbstractIT {

	private static final String FORSENDELSE_ID = "/1720847";

	@Autowired
	private BehandleAltinnMeldingHendelseService behandleAltinnMeldingHendelseService;

	@BeforeEach
	void setUp() {
		reset(); // Reset WireMock stubs between tests
		stubAzure();
	}

	@Test
	void shouldLesMeldingEventTypeCorrespondencePublishedAndUpdateForsendelseStatusToEkspedert() {
		stubFinnForsendelse();
		stubHentForsendelse();
		stubPutOppdaterForsendelse();

		sendToInnTopic(createMelding(ALTINN_EVENT_TYPE_OPPDATER_TIL_EKSPEDERT));

		behandleAltinnMeldingHendelseService.behandleAltinnMelding(getConsumerRecord());

		verify(1, getRequestedFor(urlEqualTo(HENT_FORSENDELSE_PATH + FORSENDELSE_ID)));
		verify(1, putRequestedFor(urlEqualTo(HENT_FORSENDELSE_PATH + "/oppdaterforsendelse")));
	}

	@ParameterizedTest
	@MethodSource("meldingTypes")
	void shouldLesMeldingCorrespondencerConfirmedOrReadAndUpdateJournalpostLestDato(String type) {
		stubFinnForsendelse();
		stubHentForsendelse();
		stubPutOppdaterForsendelse();
		stubPatchOppdaterDistribusjonsinfo();

		sendToInnTopic(createMelding(type));

		behandleAltinnMeldingHendelseService.behandleAltinnMelding(getConsumerRecord());

		verify(1, getRequestedFor(urlEqualTo(HENT_FORSENDELSE_PATH + FORSENDELSE_ID)));
	}

	static Stream<Arguments> meldingTypes() {
		return Stream.of(
				Arguments.of("no.altinn.correspondence.correspondencereceiverread"),
				Arguments.of("no.altinn.correspondence.correspondencereceivedconfirmed")
		);
	}

	@ParameterizedTest
	@ValueSource(strings = {CORRESPONDENCE_PUBLISH_FAILED, CORRESPONDENCE_NOTIFICATION_CREATION_FAILED})
	void shouldLesMeldingCorrespondencerPublishFailedOrNotificationCreationFailedSendMeldingToPrint(String type) {
		stubFinnForsendelse();
		stubHentForsendelse();
		stubPostDistribuerTilPrint();

		sendToInnTopic(createMelding(type));

		behandleAltinnMeldingHendelseService.behandleAltinnMelding(getConsumerRecord());

		verify(1, getRequestedFor(urlEqualTo(HENT_FORSENDELSE_PATH + FORSENDELSE_ID)));
		verify(1, postRequestedFor(urlEqualTo(DISTRIBUERTILPRINT_PATH)));
	}

	@Test
	void shouldLogMeldingWhenForsendelseStatusIsKlarForDist() {
		stubFinnForsendelse();
		stubHentForsendelseWithStatusKlarForDist();

		sendToInnTopic(createMelding(ALTINN_EVENT_TYPE_OPPDATER_TIL_EKSPEDERT));

		behandleAltinnMeldingHendelseService.behandleAltinnMelding(getConsumerRecord());

		verify(1, getRequestedFor(urlEqualTo(HENT_FORSENDELSE_PATH + FORSENDELSE_ID)));
	}

	void stubFinnForsendelse() {
		stubFor(get(urlEqualTo(FINN_FORSENDELSE_PATH))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("dokdistadmin/finnforsendelse_happy.json")));
	}

	void stubHentForsendelse() {
		stubFor(get(urlMatching(HENT_FORSENDELSE_PATH + FORSENDELSE_ID))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("dokdistadmin/hent_forsendelse_response.json")
				));
	}

	void stubHentForsendelseWithStatusKlarForDist() {
		stubFor(get(urlMatching(HENT_FORSENDELSE_PATH + FORSENDELSE_ID))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("dokdistadmin/hent_forsendelse_status_klar_for_dist.json")
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

	private void stubPutOppdaterForsendelse() {
		stubFor(put(HENT_FORSENDELSE_PATH + "/oppdaterforsendelse")
				.willReturn(aResponse()
						.withStatus(OK.value())));
	}

	private void stubPatchOppdaterDistribusjonsinfo() {
		stubFor(patch(urlPathMatching(OPPDATERDISTRIBUSJONSINFO_URL))
				.withRequestBody(containing("\"settStatusEkspedert\":false"))
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