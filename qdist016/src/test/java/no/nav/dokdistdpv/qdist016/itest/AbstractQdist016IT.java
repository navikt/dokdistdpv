package no.nav.dokdistdpv.qdist016.itest;

import jakarta.jms.Queue;
import jakarta.jms.TextMessage;
import jakarta.xml.bind.JAXBElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.jms.core.JmsTemplate;
import wiremock.org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

abstract class AbstractQdist016IT {

	private static final String FORSENDELSE_ID = "256569";
	protected static final String HENTFORSENDELSE_URL = "/rest/v1/administrerforsendelse/" + FORSENDELSE_ID;
	protected static final String OPPDATERFORSENDELSE_URL = "/rest/v1/administrerforsendelse/oppdaterforsendelse";

	@Autowired
	protected JmsTemplate jmsTemplate;

	protected static void verifyPostCorrespondenceApiV1Attachment(String fileName, String displayName, String checksum, String sendersReference) {
		verify(postRequestedFor(urlPathMatching("/altinn3/correspondence/api/v1/attachment"))
				.withRequestBody(matchingJsonPath("$[?(@.fileName == '" + fileName + "')]")
						.and(matchingJsonPath("$[?(@.displayName == '" + displayName + "')]"))
						.and(matchingJsonPath("$[?(@.checksum == '" + checksum + "')]"))
						.and(matchingJsonPath("$[?(@.sendersReference == '" + sendersReference + "')]"))
						.and(matchingJsonPath("$[?(@.resourceId == 'nav_dokumentdistribusjon_taushetsbelagtpost')]"))));
	}

	protected static void stubAltinnInitializeAttachment(String bodyFile) {
		stubAltinnInitializeAttachment(bodyFile, OK.value());
	}

	protected static void stubAltinnInitializeAttachment(String bodyFile, int status) {
		stubFor(post(urlEqualTo("/altinn3/correspondence/api/v1/attachment"))
				.willReturn(aResponse()
						.withStatus(status)
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withTransformers("response-template")
						.withBodyFile("altinn3/" + bodyFile)));
	}

	protected static void stubAltinnUploadAttachment(String bodyFile) {
		stubFor(post(urlPathMatching("/altinn3/correspondence/api/v1/attachment/.*/upload"))
				.willReturn(aResponse()
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withTransformers("response-template")
						.withBodyFile("altinn3/" + bodyFile)));
	}

	protected static void stubAltinnInitializeCorrespondence(String bodyFile) {
		stubFor(post(urlPathMatching("/altinn3/correspondence/api/v1/correspondence"))
				.willReturn(aResponse()
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withTransformers("response-template")
						.withBodyFile("altinn3/" + bodyFile)));
	}

	protected static void stubAzure() {
		stubFor(post("/azure_token")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("azure/token_response.json")));
	}

	protected static void stubNaisTexasToken() {
		stubFor(post("/nais-texas")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("nais-texas/maskinporten-token.json")));
	}


	protected static void stubDokdistGetForsendelse(String bodyFileName) {
		stubFor(get(urlEqualTo(HENTFORSENDELSE_URL))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile(bodyFileName)));
	}

	protected static void stubPutOppdaterForsendelse(int statusValue) {
		stubFor(put(urlPathMatching(OPPDATERFORSENDELSE_URL))
				.willReturn(aResponse()
						.withStatus(statusValue)
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));
	}

	protected static void stubSafPostJournalpost() {
		stubSafPostJournalpost("safGraphQlResponse-happy.json");
	}

	protected static void stubSafPostJournalpost(String bodyFile) {
		stubFor(post(urlMatching("/safgraphql"))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("saf/" + bodyFile)));
	}

	protected void sendStringMessage(Queue queue, final String message, final String callId) {
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
	protected <T> T receive(Queue queue) {
		Object response = jmsTemplate.receiveAndConvert(queue);
		if (response instanceof JAXBElement) {
			response = ((JAXBElement<?>) response).getValue();
		}
		return (T) response;
	}

	protected static String classpathToString(String classpathResource) throws IOException {
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
