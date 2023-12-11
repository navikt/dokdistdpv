package no.nav.dokdistdpv.sdist007.itest;

import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.dokdistdpv.consumer.leaderelection.LeaderElectionConsumer;
import no.nav.dokdistdpv.sdist007.itest.config.ApplicationTestConfig;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingXPath;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.patchRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

@SpringBootTest(classes = {ApplicationTestConfig.class},
		webEnvironment = RANDOM_PORT)
@EnableAutoConfiguration
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("itest")
public class Sdist007IT {

	private static final String SERVICE_CODE = "5828";
	private static final String JOURNALPOSTID1 = "123456789";
	private static final String JOURNALPOSTID2 = "999654321";
	private static final String JOURNALPOSTLISTE = "[" + JOURNALPOSTID1 + "," + JOURNALPOSTID2 + "]";
	private static final String EMPTY_JOURNALPOSTLISTE = "[]";


	public static final String DOKDISTADMIN_URL = "/rest/administrerforsendelse/";
	private static final String HENTFORSENDELSER_URL = DOKDISTADMIN_URL + "hentForsendelser.*";
	private static final String OPPDATER_AVSTEMFORSENDELSE_URL = DOKDISTADMIN_URL + "avstemforsendelser";
	private static final String FINNULESTEFORSENDELSER_URL = "/rest/journalpost/internal/sikkerhetsnivaa/finnUlesteJournalposter/DPVT/202[\\d]-.*";
	private static final String OPPDATERDISTRIBUSJONSINFO_URL = "/rest/journalpost/.*/oppdaterDistribusjonsinfo";

	@Value("${leder.host}")
	private String lederHost;

	@Autowired
	private LeaderElectionConsumer lederElection;

	@BeforeEach
	public void setupBefore() {
		System.setProperty("ELECTOR_PATH", lederHost);
		lederElection = mock(LeaderElectionConsumer.class);
		when(lederElection.isLeader()).thenReturn(true);
		WireMock.removeAllMappings();
		WireMock.reset();
		stubAzure();
	}

	@Test
	public void shouldUpdateDatoLestInDokarkivWhenCorrespondenceStatusContainsReadOrConfirmed() throws IOException {
		stubGetFinnUlesteForsendelser(JOURNALPOSTLISTE);
		stubGetHentForsendelser();
		stubAltinnCorrespondence("altinn/altinn_response.xml");
		stubPatchOppdaterDistribusjonsinfo();
		stubInsertCorrespondence("altinn/insert_correspondence_response.xml");
		stubPutOppdaterAvstemForsendelser();

		await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
			verify(exactly(1), getRequestedFor(urlPathMatching((FINNULESTEFORSENDELSER_URL))));
			verify(exactly(1), getRequestedFor(urlPathMatching(HENTFORSENDELSER_URL)));
			verify(4, postRequestedFor(urlEqualTo("/azure_token")));

			verify(2, postRequestedFor(urlEqualTo("/altinn")));
			verify(2, patchRequestedFor(urlPathMatching(OPPDATERDISTRIBUSJONSINFO_URL)));
			verify(0, putRequestedFor(urlEqualTo(OPPDATER_AVSTEMFORSENDELSE_URL)));
		});
	}

	@Test
	public void shouldSendNotificationToAltinnWhenCorrespondenceStatusDoesNotContainsReadOrConfirmed() throws IOException {
		stubGetFinnUlesteForsendelser(JOURNALPOSTLISTE);
		stubGetHentForsendelser();
		stubAltinnCorrespondence("altinn/altinn_without_read_confirmed_response.xml");
		stubInsertCorrespondence("altinn/insert_correspondence_response.xml");
		stubPatchOppdaterDistribusjonsinfo();
		stubPutOppdaterAvstemForsendelser();

		await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
			verify(exactly(1), getRequestedFor(urlPathMatching((FINNULESTEFORSENDELSER_URL))));
			verify(exactly(1), getRequestedFor(urlPathMatching(HENTFORSENDELSER_URL)));
			verify(3, postRequestedFor(urlEqualTo("/azure_token")));

			verify(4, postRequestedFor(urlEqualTo("/altinn")));
			verify(0, patchRequestedFor(urlPathMatching(OPPDATERDISTRIBUSJONSINFO_URL)));
			verify(1, putRequestedFor(urlEqualTo(OPPDATER_AVSTEMFORSENDELSE_URL)));
		});
	}

	@Test
	public void shouldLogAndReturnNullWhenUlesteJournalposterResponseIsEmpty() {
		stubGetFinnUlesteForsendelser(EMPTY_JOURNALPOSTLISTE);

		await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
			verify(exactly(1), getRequestedFor(urlPathMatching((FINNULESTEFORSENDELSER_URL))));
			verify(exactly(0), getRequestedFor(urlPathMatching(HENTFORSENDELSER_URL)));
			verify(1, postRequestedFor(urlEqualTo("/azure_token")));

			verify(0, postRequestedFor(urlEqualTo("/altinn")));
			verify(0, patchRequestedFor(urlPathMatching(OPPDATERDISTRIBUSJONSINFO_URL)));
			verify(0, putRequestedFor(urlEqualTo(OPPDATER_AVSTEMFORSENDELSE_URL)));
		});
	}

	@Test
	public void shouldLogAndReturnNullWhenCorrespondenceStatusResultThrowsException() throws IOException {
		stubGetFinnUlesteForsendelser(JOURNALPOSTLISTE);
		stubGetHentForsendelser();
		stubAltinnCorrespondence("altinn/correspondence_status_error_response.xml", INTERNAL_SERVER_ERROR);

		await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
			verify(exactly(1), getRequestedFor(urlPathMatching((FINNULESTEFORSENDELSER_URL))));
			verify(exactly(1), getRequestedFor(urlPathMatching(HENTFORSENDELSER_URL)));
			verify(2, postRequestedFor(urlEqualTo("/azure_token")));

			verify(2, postRequestedFor(urlEqualTo("/altinn")));
			verify(0, patchRequestedFor(urlPathMatching(OPPDATERDISTRIBUSJONSINFO_URL)));
			verify(0, putRequestedFor(urlEqualTo(OPPDATER_AVSTEMFORSENDELSE_URL)));
		});
	}

	private void stubPutOppdaterAvstemForsendelser() {
		stubFor(put(urlPathMatching(OPPDATER_AVSTEMFORSENDELSE_URL)).willReturn(aResponse()
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withStatus(OK.value())));
	}

	private void stubPatchOppdaterDistribusjonsinfo() {
		stubFor(patch(urlPathMatching(OPPDATERDISTRIBUSJONSINFO_URL))
				.withRequestBody(containing("\"settStatusEkspedert\":false"))
				.willReturn(aResponse()
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withStatus(OK.value())));
	}

	private void stubGetHentForsendelser() throws IOException {
		stubFor(get(urlPathMatching(HENTFORSENDELSER_URL))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody(classpathToString("__files/rdist001/hentforsendelser_happy.json"))));
	}

	private void stubAltinnCorrespondence(String bodyFile) {
		stubFor(post(urlEqualTo("/altinn"))
				.withRequestBody(matchingXPath("//ServiceCode/text()", equalTo(SERVICE_CODE)))
				.willReturn(aResponse()
						.withHeader(CONTENT_TYPE, APPLICATION_XML_VALUE)
						.withBodyFile(bodyFile)));
	}

	private void stubAltinnCorrespondence(String bodyFile, HttpStatus status) {
		stubFor(post(urlEqualTo("/altinn"))
				.willReturn(aResponse()
						.withHeader(CONTENT_TYPE, APPLICATION_XML_VALUE)
						.withStatus(status.value())
						.withBodyFile(bodyFile)));
	}

	private void stubInsertCorrespondence(String bodyFile) {
		stubFor(post(urlEqualTo("/altinn"))
				.withRequestBody(matchingXPath("//LanguageCode/text()", equalTo("1044")))
				.willReturn(aResponse()
						.withHeader(CONTENT_TYPE, APPLICATION_XML_VALUE)
						.withBodyFile(bodyFile)));
	}

	private void stubAzure() {
		stubFor(post("/azure_token")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("azure/token_response.json")));
	}

	private void stubGetFinnUlesteForsendelser(String journalpostListe) {
		stubFor(get(urlPathMatching(FINNULESTEFORSENDELSER_URL))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody(journalpostListe)));
	}

	public static String classpathToString(String path) throws IOException {
		InputStream inputStream = new ClassPathResource(path).getInputStream();
		String message = new String(inputStream.readAllBytes(), UTF_8);
		IOUtils.closeQuietly(inputStream);
		return message;
	}
}
