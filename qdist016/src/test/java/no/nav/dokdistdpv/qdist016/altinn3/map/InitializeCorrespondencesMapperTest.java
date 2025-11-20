package no.nav.dokdistdpv.qdist016.altinn3.map;

import no.nav.dokdistdpv.consumer.altinn3.api.correspondence.BaseCorrespondenceExt;
import no.nav.dokdistdpv.consumer.altinn3.api.correspondence.InitializeCorrespondenceContentExt;
import no.nav.dokdistdpv.consumer.altinn3.api.correspondence.InitializeCorrespondenceNotificationExt;
import no.nav.dokdistdpv.consumer.altinn3.api.correspondence.InitializeCorrespondencesExt;
import no.nav.dokdistdpv.consumer.rdist001.domain.DistribusjonsTypeKode;
import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse;
import no.nav.dokdistdpv.qdist016.altinn3.UploadedAttachment;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static no.nav.dokdistdpv.consumer.altinn3.Altinn3Constants.NAV_RESOURCE_ID;
import static no.nav.dokdistdpv.consumer.altinn3.api.correspondence.EmailContentType.Plain;
import static no.nav.dokdistdpv.consumer.altinn3.api.correspondence.NotificationChannelExt.EmailPreferred;
import static no.nav.dokdistdpv.consumer.altinn3.api.correspondence.NotificationTemplateExt.CustomMessage;
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

		assertCorrespondence(initializeCorrespondencesExt.correspondence(), forventetTekst);
		assertThat(initializeCorrespondencesExt.existingAttachments())
				.containsExactly(UUID.fromString(ATTACHMENT_1), UUID.fromString(ATTACHMENT_2), UUID.fromString(ATTACHMENT_3));
		assertThat(initializeCorrespondencesExt.idempotentKey().toString()).isEqualTo(BESTILLINGS_ID);
		assertThat(initializeCorrespondencesExt.recipients()).containsExactly(ISO_IEC_6523_ICD_ORGANISASJONSNUMMER + ":" + MOTTAKER_ID);
	}

	private static void assertCorrespondence(BaseCorrespondenceExt correspondence, ContentNotificationTekst forventetTekst) {
		assertThat(correspondence.resourceId()).isEqualTo(NAV_RESOURCE_ID);
		assertThat(correspondence.sendersReference()).isEqualTo(BESTILLINGS_ID);
		assertCorrespondenceContent(correspondence.content(), forventetTekst);
		assertCorrespondenceNotification(correspondence.notification(), forventetTekst);
		assertThat(correspondence.isConfidential()).isTrue();
	}

	private static void assertCorrespondenceContent(InitializeCorrespondenceContentExt content, ContentNotificationTekst forventetTekst) {
		assertThat(content.language()).isEqualTo(ISO_639_1_NORSK_BOKMAAL);
		assertThat(content.messageTitle()).isEqualTo(forventetTekst.messageTitle());
		assertThat(content.messageSummary()).isEqualTo(forventetTekst.messageSummary());
		assertThat(content.messageBody()).isEqualTo(forventetTekst.messageBody());
	}

	private static void assertCorrespondenceNotification(InitializeCorrespondenceNotificationExt notification, ContentNotificationTekst forventetTekst) {
		assertThat(notification.notificationTemplate()).isEqualTo(CustomMessage);
		assertThat(notification.emailSubject()).isEqualTo(forventetTekst.emailSubject());
		assertThat(notification.emailBody()).isEqualTo(forventetTekst.emailBody());
		assertThat(notification.emailContentType()).isEqualTo(Plain);
		assertThat(notification.smsBody()).isEqualTo(forventetTekst.smsBody());
		assertThat(notification.sendReminder()).isTrue();
		assertThat(notification.reminderEmailSubject()).isEqualTo(forventetTekst.reminderEmailSubject());
		assertThat(notification.reminderEmailBody()).isEqualTo(forventetTekst.reminderEmailBody());
		assertThat(notification.reminderEmailContentType()).isEqualTo(Plain);
		assertThat(notification.reminderSmsBody()).isEqualTo(forventetTekst.reminderSmsBody());
		assertThat(notification.notificationChannel()).isEqualTo(EmailPreferred);
		assertThat(notification.reminderNotificationChannel()).isEqualTo(EmailPreferred);
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