package no.nav.dokdistdpv.qdist016.altinn3.map;

import no.altinn.services.altinn3.openapi.domain.BaseCorrespondenceExt;
import no.altinn.services.altinn3.openapi.domain.InitializeCorrespondenceContentExt;
import no.altinn.services.altinn3.openapi.domain.InitializeCorrespondenceNotificationExt;
import no.altinn.services.altinn3.openapi.domain.InitializeCorrespondencesExt;
import no.nav.dokdistdpv.consumer.rdist001.domain.DistribusjonsTypeKode;
import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse;
import no.nav.dokdistdpv.qdist016.altinn3.UploadedAttachment;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static no.altinn.services.altinn3.openapi.domain.EmailContentType.PLAIN;
import static no.altinn.services.altinn3.openapi.domain.NotificationChannelExt.EMAIL_PREFERRED;
import static no.altinn.services.altinn3.openapi.domain.NotificationTemplateExt.CUSTOM_MESSAGE;
import static no.nav.dokdistdpv.consumer.altinn3.Altinn3Constants.NAV_RESOURCE_ID;
import static no.nav.dokdistdpv.consumer.rdist001.domain.DistribusjonsTypeKode.ANNET;
import static no.nav.dokdistdpv.consumer.rdist001.domain.DistribusjonsTypeKode.VEDTAK;
import static no.nav.dokdistdpv.consumer.rdist001.domain.DistribusjonsTypeKode.VIKTIG;
import static no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse.HOVEDDOKUMENT;
import static no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse.VEDLEGG;
import static no.nav.dokdistdpv.qdist016.altinn3.map.ContentNotificationForventedeTekster.testAnnet;
import static no.nav.dokdistdpv.qdist016.altinn3.map.ContentNotificationForventedeTekster.testVedtak;
import static no.nav.dokdistdpv.qdist016.altinn3.map.ContentNotificationForventedeTekster.testViktig;
import static no.nav.dokdistdpv.qdist016.altinn3.map.InitializeCorrespondencesMapper.ISO_639_1_NORSK_BOKMAAL;
import static no.nav.dokdistdpv.qdist016.altinn3.map.InitializeCorrespondencesMapper.ISO_IEC_6523_ICD_ORGANISASJONSNUMMER;
import static org.assertj.core.api.Assertions.assertThat;

class InitializeCorrespondencesMapperTest {
	private final static String FORSENDELSE_TITTEL = "Oppfølging av ansatt";
	private static final String MOTTAKER_ID = "986228608";
	private static final String MOTTAKER_NAVN = "YARA INTERNATIONAL ASA";
	public static final String ATTACHMENT_1 = "fa1c1bd4-e580-4cf6-8dff-5facf000bde0";
	public static final String ATTACHMENT_2 = "453e7d08-f27c-4d2b-9bf8-42c8dd433648";
	public static final String ATTACHMENT_3 = "7346899c-9814-45be-9aee-9981f26d6c57";
	public static final String BESTILLINGS_ID = "4f12f5f9-e01e-4526-b5d9-7188d9740368";

	@ParameterizedTest
	@MethodSource
	void shouldMap(DistribusjonsTypeKode distribusjonsTypeKode, ContentNotificationTekst forventetTekst) {
		HentForsendelseResponse hentForsendelseReponse = createHentForsendelseReponse(distribusjonsTypeKode);

		InitializeCorrespondencesExt initializeCorrespondencesExt = InitializeCorrespondencesMapper.map(hentForsendelseReponse, createAttachmentIds());

		assertCorrespondence(initializeCorrespondencesExt.getCorrespondence(), forventetTekst);
		assertThat(initializeCorrespondencesExt.getExistingAttachments())
				.containsExactly(UUID.fromString(ATTACHMENT_1), UUID.fromString(ATTACHMENT_2), UUID.fromString(ATTACHMENT_3));
		assertThat(initializeCorrespondencesExt.getIdempotentKey().toString()).isEqualTo(BESTILLINGS_ID);
		assertThat(initializeCorrespondencesExt.getRecipients()).containsExactly(ISO_IEC_6523_ICD_ORGANISASJONSNUMMER + ":" + MOTTAKER_ID);
	}

	private static void assertCorrespondence(BaseCorrespondenceExt correspondence, ContentNotificationTekst forventetTekst) {
		assertThat(correspondence.getResourceId()).isEqualTo(NAV_RESOURCE_ID);
		assertThat(correspondence.getSendersReference()).isEqualTo(BESTILLINGS_ID);
		assertCorrespondenceContent(correspondence.getContent(), forventetTekst);
		assertCorrespondenceNotification(correspondence.getNotification(), forventetTekst);
		assertThat(correspondence.getIsConfidential()).isTrue();
	}

	private static void assertCorrespondenceContent(InitializeCorrespondenceContentExt content, ContentNotificationTekst forventetTekst) {
		assertThat(content.getLanguage()).isEqualTo(ISO_639_1_NORSK_BOKMAAL);
		assertThat(content.getMessageTitle()).isEqualTo(forventetTekst.messageTitle());
		assertThat(content.getMessageSummary()).isEqualTo(forventetTekst.messageSummary());
		assertThat(content.getMessageBody()).isEqualTo(forventetTekst.messageBody());
	}

	private static void assertCorrespondenceNotification(InitializeCorrespondenceNotificationExt notification, ContentNotificationTekst forventetTekst) {
		assertThat(notification.getNotificationTemplate()).isEqualTo(CUSTOM_MESSAGE);
		assertThat(notification.getEmailSubject()).isEqualTo(forventetTekst.emailSubject());
		assertThat(notification.getEmailBody()).isEqualTo(forventetTekst.emailBody());
		assertThat(notification.getEmailContentType()).isEqualTo(PLAIN);
		assertThat(notification.getSmsBody()).isEqualTo(forventetTekst.smsBody());
		assertThat(notification.getSendReminder()).isTrue();
		assertThat(notification.getReminderEmailSubject()).isEqualTo(forventetTekst.reminderEmailSubject());
		assertThat(notification.getReminderEmailBody()).isEqualTo(forventetTekst.reminderEmailBody());
		assertThat(notification.getEmailContentType()).isEqualTo(PLAIN);
		assertThat(notification.getReminderSmsBody()).isEqualTo(forventetTekst.reminderSmsBody());
		assertThat(notification.getNotificationChannel()).isEqualTo(EMAIL_PREFERRED);
		assertThat(notification.getReminderNotificationChannel()).isEqualTo(EMAIL_PREFERRED);
	}

	private static Stream<Arguments> shouldMap() {
		return Stream.of(
				Arguments.of(VEDTAK, testVedtak()),
				Arguments.of(VIKTIG, testViktig()),
				Arguments.of(null, testViktig()),
				Arguments.of(ANNET, testAnnet())
		);
	}

	private static HentForsendelseResponse createHentForsendelseReponse(DistribusjonsTypeKode distribusjonsTypeKode) {
		HentForsendelseResponse.Mottaker mottaker = new HentForsendelseResponse.Mottaker(MOTTAKER_ID, MOTTAKER_NAVN, "ORGANISASJON");

		return new HentForsendelseResponse(
				1L,
				BESTILLINGS_ID,
				null,
				"KLAR_FOR_DIST",
				"SYK",
				FORSENDELSE_TITTEL,
				mottaker,
				null,
				null,
				createDokumenter(),
				null,
				distribusjonsTypeKode
		);
	}

	private static List<HentForsendelseResponse.Dokument> createDokumenter() {
		return List.of(
				new HentForsendelseResponse.Dokument(HOVEDDOKUMENT, "dokumentObjektReferanse1", "100", null),
				new HentForsendelseResponse.Dokument(VEDLEGG, "dokumentObjektReferanse2", "101", null),
				new HentForsendelseResponse.Dokument(VEDLEGG, "dokumentObjektReferanse3", "102", null)
		);
	}

	private static List<UploadedAttachment> createAttachmentIds() {
		return List.of(
				new UploadedAttachment(ATTACHMENT_1, 1),
				new UploadedAttachment(ATTACHMENT_2, 2),
				new UploadedAttachment(ATTACHMENT_3, 3)
		);
	}
}