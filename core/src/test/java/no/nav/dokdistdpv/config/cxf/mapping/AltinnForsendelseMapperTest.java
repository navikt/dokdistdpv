package no.nav.dokdistdpv.config.cxf.mapping;

import no.altinn.correspondenceagencyexternalaec.BinaryAttachmentV2;
import no.altinn.correspondenceagencyexternalaec.ExternalContentV2;
import no.altinn.correspondenceagencyexternalaec.InsertCorrespondenceV2;
import no.altinn.correspondenceagencyexternalaec.Notification;
import no.altinn.correspondenceagencyexternalaec.NotificationBEList;
import no.altinn.correspondenceagencyexternalaec.ReceiverEndPoint;
import no.nav.dokdistdpv.consumer.rdist001.domain.DistribusjonsTypeKode;
import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static no.altinn.correspondenceagencyexternalaec.AttachmentFunctionType.UNSPECIFIED;
import static no.altinn.correspondenceagencyexternalaec.TransportType.EMAIL_PREFERRED;
import static no.altinn.correspondenceagencyexternalaec.UserTypeRestriction.DEFAULT;
import static no.nav.dokdistdpv.config.cxf.mapping.AltinnForsendelseMapper.FROM_ADDRESS;
import static no.nav.dokdistdpv.config.cxf.mapping.AltinnForsendelseMapper.LANGUAGE_CODE_BOKMAAL;
import static no.nav.dokdistdpv.config.cxf.mapping.AltinnForsendelseMapper.mapToCorrespondence;
import static no.nav.dokdistdpv.config.cxf.mapping.ContentMapper.MESSAGE_TITLE_ANNET;
import static no.nav.dokdistdpv.config.cxf.mapping.ContentMapper.MESSAGE_TITLE_VEDTAK;
import static no.nav.dokdistdpv.config.cxf.mapping.ContentMapper.MESSAGE_TITLE_VIKTIG;
import static no.nav.dokdistdpv.config.cxf.mapping.NotificationsMapper.NOTIFICATION_FOR_ANNET;
import static no.nav.dokdistdpv.config.cxf.mapping.NotificationsMapper.NOTIFICATION_FOR_VEDTAK_VIKTIG_ELLER_IKKE_SATT;
import static no.nav.dokdistdpv.consumer.rdist001.domain.DistribusjonsTypeKode.ANNET;
import static no.nav.dokdistdpv.consumer.rdist001.domain.DistribusjonsTypeKode.VEDTAK;
import static no.nav.dokdistdpv.consumer.rdist001.domain.DistribusjonsTypeKode.VIKTIG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class AltinnForsendelseMapperTest {

	private final static String FORSENDELSETITTEL = "Tittel på forsendelse";
	private static final String SERVICE_CODE = "5828";
	private static final String SERVICE_EDITION_CODE = "1";
	private static final String REPORTEE = "123456789";
	private static final String serviceCode = SERVICE_CODE;
	private static final String serviceEditionCode = SERVICE_EDITION_CODE;

	@ParameterizedTest
	@MethodSource("provideMessageTitleMessageBodyAndNotificationTypeForDistribusjonstype")
	void shouldMapAltinnForsendelse(
			DistribusjonsTypeKode distribusjonsTypeKode,
			String expectedMessageTitle,
			String expectedMessageBody,
			String expectedNotificationType
	) {
		HentForsendelseResponse forsendelse = createHentForsendelseReponse(distribusjonsTypeKode);
		List<AltinnDokument> dokumenter = createDokumenter();

		InsertCorrespondenceV2 result = mapToCorrespondence(forsendelse, dokumenter, serviceCode, serviceEditionCode);

		assertEquals(SERVICE_CODE, result.getServiceCode());
		assertEquals(SERVICE_EDITION_CODE, result.getServiceEdition());
		assertEquals(REPORTEE, result.getReportee());

		assertContent(expectedMessageTitle, expectedMessageBody, forsendelse, dokumenter, result);
		assertNotifications(expectedNotificationType, result);
	}

	private void assertNotifications(
			String expectedNotificationType,
			InsertCorrespondenceV2 result
	) {
		NotificationBEList notificationList = result.getNotifications();
		assertEquals(1, notificationList.getNotification().size());
		Notification notification = notificationList.getNotification().get(0);
		assertEquals(FROM_ADDRESS, notification.getFromAddress());
		assertEquals(LANGUAGE_CODE_BOKMAAL, notification.getLanguageCode());
		assertEquals(expectedNotificationType, notification.getNotificationType());

		assertEquals(1, notification.getReceiverEndPoints().getReceiverEndPoint().size());
		ReceiverEndPoint receiverEndPoint = notification.getReceiverEndPoints().getReceiverEndPoint().get(0);
		assertEquals(EMAIL_PREFERRED, receiverEndPoint.getTransportType());
		assertNull(receiverEndPoint.getReceiverAddress());
	}

	private void assertContent(
			String expectedMessageTitle,
			String expectedMessageBody,
			HentForsendelseResponse forsendelse,
			List<AltinnDokument> dokumenter,
			InsertCorrespondenceV2 result
	) {
		ExternalContentV2 content = result.getContent();
		assertEquals(LANGUAGE_CODE_BOKMAAL, content.getLanguageCode());
		assertEquals(expectedMessageTitle, content.getMessageTitle());
		assertEquals(expectedMessageBody, content.getMessageBody());

		assertEquals(3, content.getAttachments().getBinaryAttachments().getBinaryAttachmentV2().size());
		for (int i = 0; i < forsendelse.dokumenter().size(); i++) {
			BinaryAttachmentV2 attachment = content.getAttachments().getBinaryAttachments().getBinaryAttachmentV2().get(i);
			assertEquals(DEFAULT, attachment.getDestinationType());
			assertEquals(UNSPECIFIED, attachment.getFunctionType());
			assertFalse(attachment.isEncrypted());
			assertEquals(forsendelse.dokumenter().get(i).arkivDokumentInfoId() + dokumenter.get(i).tittel() + ".pdf", attachment.getFileName());
			assertEquals(dokumenter.get(i).tittel(), attachment.getName());
			assertEquals(dokumenter.get(i).pdf(), attachment.getData());
			assertEquals(forsendelse.dokumenter().get(i).dokumentObjektReferanse(), attachment.getSendersReference());
		}

		assertNull(content.getCustomMessageData());
	}

	private static Stream<Arguments> provideMessageTitleMessageBodyAndNotificationTypeForDistribusjonstype() {
		return Stream.of(
				Arguments.of(VEDTAK, MESSAGE_TITLE_VEDTAK, String.format("Du har fått et vedtak som gjelder %s.", FORSENDELSETITTEL), NOTIFICATION_FOR_VEDTAK_VIKTIG_ELLER_IKKE_SATT),
				Arguments.of(VIKTIG, MESSAGE_TITLE_VIKTIG, String.format("Du har fått et brev som du må lese: %s.", FORSENDELSETITTEL), NOTIFICATION_FOR_VEDTAK_VIKTIG_ELLER_IKKE_SATT),
				Arguments.of(ANNET, MESSAGE_TITLE_ANNET, String.format("Du har fått en melding som gjelder %s.", FORSENDELSETITTEL), NOTIFICATION_FOR_ANNET),
				Arguments.of(null, MESSAGE_TITLE_VIKTIG, String.format("Du har fått et brev som du må lese: %s.", FORSENDELSETITTEL), NOTIFICATION_FOR_VEDTAK_VIKTIG_ELLER_IKKE_SATT)
		);
	}

	private HentForsendelseResponse createHentForsendelseReponse(DistribusjonsTypeKode distribusjonsTypeKode) {

		List<HentForsendelseResponse.Dokument> dokumenter = List.of(
				new HentForsendelseResponse.Dokument(null, "dokumentObjektReferanse1", "arkivDokumentInfoId1", null),
				new HentForsendelseResponse.Dokument(null, "dokumentObjektReferanse2", "arkivDokumentInfoId2", null),
				new HentForsendelseResponse.Dokument(null, "dokumentObjektReferanse3", "arkivDokumentInfoId3", null)
		);

		HentForsendelseResponse.Mottaker mottaker = new HentForsendelseResponse.Mottaker(REPORTEE, null, null);

		return new HentForsendelseResponse(
				"bestillingsId",
				"konversasjonId",
				"bestillendeFagsystem",
				"modus",
				"forsendelseStatus",
				"tema",
				FORSENDELSETITTEL,
				"batchId",
				"dokumentProdApp",
				mottaker,
				null,
				null,
				dokumenter,
				null,
				distribusjonsTypeKode
		);
	}

	private List<AltinnDokument> createDokumenter() {
		var pdf1 = "PDF for dokument 1".getBytes(UTF_8);
		var pdf2 = "PDF for dokument 2".getBytes(UTF_8);
		var pdf3 = "PDF for dokument 3".getBytes(UTF_8);
		var tittel1 = "Hoveddokument";
		var tittel2 = "Vedlegg 1";
		var tittel3 = "Vedlegg 2";

		return List.of(
				new AltinnDokument(tittel1, pdf1),
				new AltinnDokument(tittel2, pdf2),
				new AltinnDokument(tittel3, pdf3)
		);
	}

}