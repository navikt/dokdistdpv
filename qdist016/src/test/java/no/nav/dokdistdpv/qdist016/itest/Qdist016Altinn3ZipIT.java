package no.nav.dokdistdpv.qdist016.itest;

import jakarta.jms.Queue;
import jakarta.jms.TextMessage;
import lombok.SneakyThrows;
import no.nav.dokdistdpv.cloudstorage.DokDistDokumentFraBucket;
import no.nav.dokdistdpv.cloudstorage.EncryptedBucketStorage;
import no.nav.dokdistdpv.utils.MDCOperations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import wiremock.org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@DirtiesContext
@EnableAutoConfiguration
@SpringBootTest(
		classes = {ApplicationTestConfig.class},
		webEnvironment = RANDOM_PORT,
		properties = {"dokdistdpv.qdist016.altinn3=true"}
)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("itest")
public class Qdist016Altinn3ZipIT {

	private static final String FORSENDELSE_ID = "256569";
	private static final String HENTFORSENDELSE_URL = "/rest/v1/administrerforsendelse/" + FORSENDELSE_ID;
	private static final String OPPDATERFORSENDELSE_URL = "/rest/v1/administrerforsendelse/oppdaterforsendelse";
	private static final String QDIST016_MELDING = "__files/qdist016-happy.xml";
	private static final byte[] DOKUMENT = "Dokument".getBytes();

	@MockitoBean
	private EncryptedBucketStorage encryptedBucketStorage;

	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	private Queue qdist016;

	@BeforeEach
	public void setupBefore() {
		stubAzure();
		stubNaisTexasToken();
	}

	@SneakyThrows
	@Test
	public void shouldProcessForsendelseOgSendTilAltinnMelding() {
		stubDokdistGetForsendelse("administrerForsendelse/forsendelse-11-vedlegg-happy.json");
		stubPutOppdaterForsendelse(OK.value());
		stubDownloadObjects();
		stubSafPostJournalpost("saf-graphql-11-vedlegg-happy.json");
		stubAltinnInitializeAttachment("initialize-attachment-ok.json");
		stubAltinnUploadAttachment("upload-attachment-ok.json");
		stubAltinnInitializeCorrespondence("initialize-correspondence-ok.json");

		sendStringMessage(qdist016, classpathToString(QDIST016_MELDING), MDCOperations.getCallId());

		await().atMost(10, SECONDS).untilAsserted(() -> {
			verify(1, getRequestedFor(urlEqualTo(HENTFORSENDELSE_URL)));
			verify(1, putRequestedFor(urlPathEqualTo(OPPDATERFORSENDELSE_URL)));
			verify(1, postRequestedFor(urlEqualTo("/safgraphql")));
			verify(1, postRequestedFor(urlEqualTo("/altinn3/correspondence/api/v1/correspondence"))
					.withRequestBody(matchingJsonPath("$[?(@.existingAttachments.size() == 2)]")));
			verify(2, postRequestedFor(urlPathMatching("/altinn3/correspondence/api/v1/attachment/.*/upload")));
		});
	}

	private void stubDownloadObjects() {
		when(encryptedBucketStorage.downloadObject(anyString(), anyString()))
				.thenAnswer(invocationOnMock ->
						DokDistDokumentFraBucket.builder().pdf(DOKUMENT).objectName(invocationOnMock.getArgument(0)).build());
	}

	private void stubDokdistGetForsendelse(String bodyFileName) {
		stubFor(get(urlEqualTo(HENTFORSENDELSE_URL))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile(bodyFileName)));
	}

	private void stubPutOppdaterForsendelse(int statusValue) {
		stubFor(put(urlPathMatching(OPPDATERFORSENDELSE_URL))
				.willReturn(aResponse()
						.withStatus(statusValue)
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
	}

	private void stubSafPostJournalpost(String bodyFile) {
		stubFor(post(urlMatching("/safgraphql"))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("saf/" + bodyFile)));
	}

	private void stubAltinnInitializeAttachment(String bodyFile) {
		stubAltinnInitializeAttachment(bodyFile, OK.value());
	}

	private void stubAltinnInitializeAttachment(String bodyFile, int status) {
		stubFor(post(urlEqualTo("/altinn3/correspondence/api/v1/attachment"))
				.willReturn(aResponse()
						.withStatus(status)
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withTransformers("response-template")
						.withBodyFile("altinn3/" + bodyFile)));
	}

	private void stubAltinnUploadAttachment(String bodyFile) {
		stubFor(post(urlPathMatching("/altinn3/correspondence/api/v1/attachment/.*/upload"))
				.willReturn(aResponse()
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withTransformers("response-template")
						.withBodyFile("altinn3/" + bodyFile)));
	}

	private void stubAltinnInitializeCorrespondence(String bodyFile) {
		stubFor(post(urlPathMatching("/altinn3/correspondence/api/v1/correspondence"))
				.willReturn(aResponse()
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withTransformers("response-template")
						.withBodyFile("altinn3/" + bodyFile)));
	}

	private void stubAzure() {
		stubFor(post("/azure_token")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("azure/token_response.json")));
	}

	private void stubNaisTexasToken() {
		stubFor(post("/nais-texas")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("nais-texas/maskinporten-token.json")));
	}

	private void sendStringMessage(Queue queue, final String message, final String callId) {
		jmsTemplate.send(queue, session -> {
			TextMessage msg = session.createTextMessage();
			msg.setText(message);
			if (callId != null) {
				msg.setStringProperty("callId", callId);
			}
			return msg;
		});
	}

	public static String classpathToString(String classpathResource) throws IOException {
		try {
			InputStream inputStream = new ClassPathResource(classpathResource).getInputStream();
			String message = IOUtils.toString(inputStream, UTF_8);
			IOUtils.closeQuietly(inputStream);
			return message;
		} catch (IOException e) {
			throw new IOException(format("Kunne ikke åpne classpath-ressurs %s", classpathResource), e);
		}
	}
}
