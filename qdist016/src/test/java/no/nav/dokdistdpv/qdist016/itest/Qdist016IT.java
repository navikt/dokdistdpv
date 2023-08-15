package no.nav.dokdistdpv.qdist016.itest;

import com.github.tomakehurst.wiremock.client.WireMock;
import jakarta.jms.Queue;
import jakarta.jms.TextMessage;
import jakarta.xml.bind.JAXBElement;
import lombok.SneakyThrows;
import no.nav.dokdistdpv.cloudstorage.DokDistDokumentFraBucket;
import no.nav.dokdistdpv.cloudstorage.EncryptedBucketStorage;
import no.nav.dokdistdpv.exception.ObjectDownloadFailedException;
import no.nav.dokdistdpv.utils.MDCOperations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;
import wiremock.org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@EnableAutoConfiguration
@SpringBootTest(
		classes = {ApplicationTestConfig.class},
		webEnvironment = RANDOM_PORT
)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("itest")
public class Qdist016IT {
	private static final String FORSENDELSE_ID = "256569";

	public static final String DOKUMENT_OBJEKT_REFERANSE_HOVEDDOK = "dokumentObjektReferanseHoveddok";
	public static final String DOKUMENT_OBJEKT_REFERANSE_VEDLEGG1 = "dokumentObjektReferanseVedlegg1";
	public static final String DOKUMENT_OBJEKT_REFERANSE_VEDLEGG2 = "dokumentObjektReferanseVedlegg2";
	public static final String HOVEDDOK_TEST_CONTENT = "HOVEDDOK_TEST_CONTENT";
	public static final String VEDLEGG1_TEST_CONTENT = "VEDLEGG1_TEST_CONTENT";
	public static final String VEDLEGG2_TEST_CONTENT = "VEDLEGG2_TEST_CONTENT";
	private static final String HENTFORSENDELSE_URL = "/rest/v1/administrerforsendelse/" + FORSENDELSE_ID;
	private static final String OPPDATERFORSENDELSE_URL = "/rest/v1/administrerforsendelse/oppdaterforsendelse";


	@Autowired
	private EncryptedBucketStorage encryptedBucketStorage;

	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	private Queue qdist016;

	@Autowired
	private Queue qdist016FunksjonellFeil;

	@Autowired
	private Queue qdist016TekniskFeil;

	@BeforeEach
	public void setupBefore() {
		WireMock.reset();
		Mockito.reset(encryptedBucketStorage);
		stubAzure();
	}

	@SneakyThrows
	@Test
	public void shouldProcessForsendelseOgSendTilDigitalPost() {
		stubDokdistGetForsendelse("administrerForsendelse/getForsendelse-happy.json");
		stubPutOppdaterForsendelse(OK.value());
		stubDownloadObject();
		stubSafPostJournalpost();
		stubAltinnInsertCorrespondence("altinn/altinnResponse.xml");

		sendStringMessage(qdist016, classpathToString("__files/qdist016-happy.xml"), MDCOperations.getCallId());

		await().atMost(10, SECONDS).untilAsserted(() -> {
			verify(1, getRequestedFor(urlEqualTo(HENTFORSENDELSE_URL)));
			verify(2, putRequestedFor(urlPathEqualTo(OPPDATERFORSENDELSE_URL)));
			verify(1, postRequestedFor(urlEqualTo("/safgraphql")));
			verify(1, postRequestedFor(urlEqualTo("/altinn")));
			verify(4, postRequestedFor(urlEqualTo("/azure_token")));

			Mockito.verify(encryptedBucketStorage, times(1)).downloadObject(eq(DOKUMENT_OBJEKT_REFERANSE_HOVEDDOK), anyString());
			Mockito.verify(encryptedBucketStorage, times(1)).downloadObject(eq(DOKUMENT_OBJEKT_REFERANSE_VEDLEGG1), anyString());
			Mockito.verify(encryptedBucketStorage, times(1)).downloadObject(eq(DOKUMENT_OBJEKT_REFERANSE_VEDLEGG2), anyString());
		});

	}

	@Test
	@SneakyThrows
	public void shouldFailToTekniskFeilQueueOnAdministrerForsendelseServerError() {
		stubDokdistGetForsendelse(INTERNAL_SERVER_ERROR);
		sendStringMessage(qdist016, classpathToString("__files/qdist016-happy.xml"), MDCOperations.getCallId());

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(qdist016TekniskFeil));
	}

	@Test
	@SneakyThrows
	public void shouldFailToFunksjonellFeilQueueOnAdministrerForsendelseClientError() {
		stubDokdistGetForsendelse(NOT_FOUND);
		sendStringMessage(qdist016, classpathToString("__files/qdist016-happy.xml"), MDCOperations.getCallId());

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(qdist016FunksjonellFeil));
	}

	@Test
	@SneakyThrows
	public void shouldFailToFunksjonellFeilQueueOnForsendelseWithStatusNotKlarForDist() {
		stubDokdistGetForsendelse("administrerForsendelse/getForsendelse-oversendt.json");

		sendStringMessage(qdist016, classpathToString("__files/qdist016-happy.xml"), MDCOperations.getCallId());

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(qdist016FunksjonellFeil));
	}

	@Test
	@SneakyThrows
	public void shouldFailToTekniskFeilQueueOnHentHoveddokumentNotFoundInBucket() {
		stubDokdistGetForsendelse("administrerForsendelse/getForsendelse-happy.json");
		stubPutOppdaterForsendelse(OK.value());

		stubDownloadObjectHoveddokumentNotFound();

		sendStringMessage(qdist016, classpathToString("__files/qdist016-happy.xml"), MDCOperations.getCallId());

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(qdist016TekniskFeil));
	}

	@Test
	@SneakyThrows
	public void shouldFailToTekniskFeilQueueOnHentVedleggNotFoundInBucket() {
		stubDokdistGetForsendelse("administrerForsendelse/getForsendelse-happy-ikke-joark.json");
		stubPutOppdaterForsendelse(OK.value());

		stubDownloadObjectVedleggNotFound();

		sendStringMessage(qdist016, classpathToString("__files/qdist016-happy.xml"), MDCOperations.getCallId());

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(qdist016TekniskFeil));
	}

	@Test
	@SneakyThrows
	public void shouldFailToTekniskFeilQueueOnHentVedleggWhenJournalpostNotFoundInSaf() {
		stubDokdistGetForsendelse("administrerForsendelse/getForsendelse-happy.json");
		stubPutOppdaterForsendelse(OK.value());
		stubDownloadObject();

		stubSafPostJournalpostNotFound();

		sendStringMessage(qdist016, classpathToString("__files/qdist016-happy.xml"), MDCOperations.getCallId());

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(qdist016FunksjonellFeil));
	}

	@Test
	@SneakyThrows
	public void shouldFailToTekniskFeilQueueOnHentVedleggWhenSafServerError() {
		stubDokdistGetForsendelse("administrerForsendelse/getForsendelse-happy.json");
		stubPutOppdaterForsendelse(OK.value());
		stubDownloadObject();

		stubSafPostJournalpostServerError();

		sendStringMessage(qdist016, classpathToString("__files/qdist016-happy.xml"), MDCOperations.getCallId());

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(qdist016TekniskFeil));
	}

	@Test
	@SneakyThrows
	public void shouldFailToFunksjonellFeilQueueOnAltinnException() {
		stubDokdistGetForsendelse("administrerForsendelse/getForsendelse-happy.json");
		stubPutOppdaterForsendelse(OK.value());
		stubDownloadObject();
		stubSafPostJournalpost();

		stubAltinnInsertCorrespondence("altinn/altinnErrorResponse.xml");

		sendStringMessage(qdist016, classpathToString("__files/qdist016-happy.xml"), MDCOperations.getCallId());

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(qdist016FunksjonellFeil));
	}

	@Test
	@SneakyThrows
	public void shouldFailToFunksjonellFeilQueueOnAltinnNotOkStatus() {
		stubDokdistGetForsendelse("administrerForsendelse/getForsendelse-happy.json");
		stubPutOppdaterForsendelse(OK.value());
		stubDownloadObject();
		stubSafPostJournalpost();

		stubAltinnInsertCorrespondence("altinn/altinnResponseRejected.xml");

		sendStringMessage(qdist016, classpathToString("__files/qdist016-happy.xml"), MDCOperations.getCallId());

		await().atMost(100, SECONDS).untilAsserted(() -> assertMessageOnQueue(qdist016FunksjonellFeil));
	}

	@SneakyThrows
	@Test
	public void shouldFailToFunksjonellQueueOnPutForsendelseNotFound() {

		stubDokdistGetForsendelse("administrerForsendelse/getForsendelse-happy.json");
		stubPutOppdaterForsendelse(NOT_FOUND.value());

		stubDownloadObject();
		stubSafPostJournalpost();
		stubAltinnInsertCorrespondence("altinn/altinnResponse.xml");
		sendStringMessage(qdist016, classpathToString("__files/qdist016-happy.xml"), MDCOperations.getCallId());

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(qdist016FunksjonellFeil));
	}

	@SneakyThrows
	@Test
	public void shouldFailToTekniskFeilQueueOnPutForsendelseServerError() {

		stubDokdistGetForsendelse("administrerForsendelse/getForsendelse-happy.json");
		stubPutOppdaterForsendelse(INTERNAL_SERVER_ERROR.value());

		stubDownloadObject();
		stubSafPostJournalpost();
		stubAltinnInsertCorrespondence("altinn/altinnResponse.xml");
		sendStringMessage(qdist016, classpathToString("__files/qdist016-happy.xml"), MDCOperations.getCallId());

		await().atMost(10, SECONDS).untilAsserted(() -> assertMessageOnQueue(qdist016TekniskFeil));
	}

	@SneakyThrows
	private void assertMessageOnQueue(Queue queue) {
		String message = receive(queue);
		assertNotNull(message);
		assertEquals(message, classpathToString("__files/qdist016-happy.xml"));
	}

	private void stubDownloadObject() {
		when(encryptedBucketStorage.downloadObject(eq(DOKUMENT_OBJEKT_REFERANSE_HOVEDDOK), anyString()))
				.thenReturn(DokDistDokumentFraBucket.builder().pdf(HOVEDDOK_TEST_CONTENT.getBytes()).build());
		when(encryptedBucketStorage.downloadObject(eq(DOKUMENT_OBJEKT_REFERANSE_VEDLEGG1), anyString()))
				.thenReturn(DokDistDokumentFraBucket.builder().pdf(VEDLEGG1_TEST_CONTENT.getBytes()).build());
		when(encryptedBucketStorage.downloadObject(eq(DOKUMENT_OBJEKT_REFERANSE_VEDLEGG2), anyString()))
				.thenReturn(DokDistDokumentFraBucket.builder().pdf(VEDLEGG2_TEST_CONTENT.getBytes()).build());
	}

	private void stubDownloadObjectHoveddokumentNotFound() {
		when(encryptedBucketStorage.downloadObject(eq(DOKUMENT_OBJEKT_REFERANSE_HOVEDDOK), anyString()))
				.thenThrow(new ObjectDownloadFailedException("Object not found", new Throwable()));
	}

	private void stubDownloadObjectVedleggNotFound() {
		when(encryptedBucketStorage.downloadObject(eq(DOKUMENT_OBJEKT_REFERANSE_HOVEDDOK), anyString()))
				.thenReturn(DokDistDokumentFraBucket.builder().pdf(HOVEDDOK_TEST_CONTENT.getBytes()).build());
		when(encryptedBucketStorage.downloadObject(eq(DOKUMENT_OBJEKT_REFERANSE_VEDLEGG1), anyString()))
				.thenThrow(new ObjectDownloadFailedException("Object not found", new Throwable()));
	}

	private void stubDokdistGetForsendelse(String bodyFileName) {
		stubFor(get(urlEqualTo(HENTFORSENDELSE_URL))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile(bodyFileName)));
	}

	private void stubDokdistGetForsendelse(HttpStatus status) {
		stubFor(get(urlEqualTo(HENTFORSENDELSE_URL))
				.willReturn(aResponse()
						.withStatus(status.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
	}

	private void stubPutOppdaterForsendelse(int statusValue) {
		stubFor(put(urlPathMatching(OPPDATERFORSENDELSE_URL))
				.willReturn(aResponse()
						.withStatus(statusValue)
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
	}

	private void stubSafPostJournalpost() {
		stubFor(post(urlMatching("/safgraphql"))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("saf/safGraphQlResponse-happy.json")));
	}

	private void stubSafPostJournalpostNotFound() {
		stubFor(post(urlMatching("/safgraphql"))
				.willReturn(aResponse()
						.withStatus(NOT_FOUND.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
	}

	private void stubSafPostJournalpostServerError() {
		stubFor(post(urlMatching("/safgraphql"))
				.willReturn(aResponse()
						.withStatus(INTERNAL_SERVER_ERROR.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
	}

	private void stubAltinnInsertCorrespondence(String bodyFile) {
		stubFor(post(urlEqualTo("/altinn"))
				.willReturn(aResponse()
						.withHeader(CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
						.withBodyFile(bodyFile)));
	}

	private void stubAzure() {
		stubFor(post("/azure_token")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("azure/token_response.json")));
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

	@SuppressWarnings("unchecked")
	private <T> T receive(Queue queue) {
		Object response = jmsTemplate.receiveAndConvert(queue);
		if (response instanceof JAXBElement) {
			response = ((JAXBElement) response).getValue();
		}
		return (T) response;
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
