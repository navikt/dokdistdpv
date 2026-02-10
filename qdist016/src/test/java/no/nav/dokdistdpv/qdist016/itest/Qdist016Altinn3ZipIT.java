package no.nav.dokdistdpv.qdist016.itest;

import jakarta.jms.Queue;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.OK;

@DirtiesContext
@EnableAutoConfiguration
@SpringBootTest(
		classes = {ApplicationTestConfig.class},
		webEnvironment = RANDOM_PORT,
		properties = {"dokdistdpv.qdist016.altinn3=true"}
)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("itest")
public class Qdist016Altinn3ZipIT extends AbstractQdist016IT {

	private static final String QDIST016_MELDING = "__files/qdist016-happy.xml";
	private static final byte[] DOKUMENT = "Dokument".getBytes();

	@MockitoBean
	private EncryptedBucketStorage encryptedBucketStorage;

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
			verifyPostCorrespondenceApiV1Attachment("100_forsendelseTittel.pdf", "forsendelseTittel", "160f00299ecc01a8dbdf5b7cb9e91da1", "450e8400-e29b-41d4-a716-446655441111");
			verify(2, postRequestedFor(urlEqualTo("/altinn3/correspondence/api/v1/attachment")));
			verify(2, postRequestedFor(urlPathMatching("/altinn3/correspondence/api/v1/attachment/.*/upload")));
			verify(postRequestedFor(urlEqualTo("/altinn3/correspondence/api/v1/correspondence"))
					.withRequestBody(matchingJsonPath("$[?(@.existingAttachments.size() == 2)]")));
			verify(putRequestedFor(urlPathEqualTo(OPPDATERFORSENDELSE_URL))
					.withRequestBody(matchingJsonPath("$[?(@.forsendelseStatus == 'OVERSENDT')]")));
		});
	}

	private void stubDownloadObjects() {
		when(encryptedBucketStorage.downloadObject(anyString(), anyString()))
				.thenAnswer(invocationOnMock ->
						DokDistDokumentFraBucket.builder().pdf(DOKUMENT).objectName(invocationOnMock.getArgument(0)).build());
	}
}
