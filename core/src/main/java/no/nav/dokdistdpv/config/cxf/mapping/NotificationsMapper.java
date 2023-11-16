package no.nav.dokdistdpv.config.cxf.mapping;

import no.altinn.correspondenceagencyexternalaec.Notification;
import no.altinn.correspondenceagencyexternalaec.NotificationBEList;
import no.altinn.correspondenceagencyexternalaec.ReceiverEndPoint;
import no.altinn.correspondenceagencyexternalaec.ReceiverEndPointBEList;
import no.altinn.correspondenceagencyexternalaec.TextToken;
import no.altinn.correspondenceagencyexternalaec.TextTokenSubstitutionBEList;
import no.nav.dokdistdpv.consumer.rdist001.domain.DistribusjonsTypeKode;
import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse;

import static no.altinn.correspondenceagencyexternalaec.TransportType.EMAIL_PREFERRED;
import static no.nav.dokdistdpv.config.cxf.mapping.AltinnForsendelseMapper.FROM_ADDRESS;
import static no.nav.dokdistdpv.config.cxf.mapping.AltinnForsendelseMapper.LANGUAGE_CODE_BOKMAAL;
import static no.nav.dokdistdpv.consumer.rdist001.domain.DistribusjonsTypeKode.ANNET;
import static no.nav.dokdistdpv.consumer.rdist001.domain.DistribusjonsTypeKode.VIKTIG;

public class NotificationsMapper {

	public final static String NOTIFICATION_FOR_ANNET = "VarselDPVUtenRevarsel";
	public final static String NOTIFICATION_FOR_VEDTAK_VIKTIG_ELLER_IKKE_SATT = "VarselDPVMedRevarsel";
	public static final String NOTIFICATION_TEXT_FORMAT = "%s %s har mottatt %s «%s» fra NAV i Altinn. " +
			"For å få tilgang til %s må noen i %s få tilgang til tjenesten «Taushetsbelagt post fra NAV» eller rollen «Taushetsbelagt post» i Altinn. " +
			"Les mer om tildeling av tilganger og roller på Altinn.";

	public static NotificationBEList mapNotifications(HentForsendelseResponse forsendelse) {
		NotificationBEList notificationList = new NotificationBEList();

		Notification notification = new Notification();
		notification.setFromAddress(FROM_ADDRESS);
		notification.setLanguageCode(LANGUAGE_CODE_BOKMAAL);
		notification.setNotificationType(mapNotificationType(forsendelse.distribusjonstype()));
		notification.setTextTokens(mapTextTokens(generateNotificationContentFor(forsendelse)));
		notification.setReceiverEndPoints(mapReceiverEndpoints());
		notificationList.getNotification().add(notification);

		return notificationList;
	}

	private static TextTokenSubstitutionBEList mapTextTokens(String notificationTextContent) {
		TextTokenSubstitutionBEList textTokenSubstitutionBEList = new TextTokenSubstitutionBEList();

		TextToken textToken = new TextToken();
		textToken.setTokenNum(1);
		textToken.setTokenValue(notificationTextContent);
		textTokenSubstitutionBEList.getTextToken().add(textToken);

		return textTokenSubstitutionBEList;
	}

	private static ReceiverEndPointBEList mapReceiverEndpoints() {
		ReceiverEndPointBEList receiverList = new ReceiverEndPointBEList();

		ReceiverEndPoint receiver = new ReceiverEndPoint();
		receiver.setTransportType(EMAIL_PREFERRED);
		receiverList.getReceiverEndPoint().add(receiver);

		return receiverList;
	}

	private static String mapNotificationType(DistribusjonsTypeKode distribusjonsTypeKode) {
		if (ANNET.equals(distribusjonsTypeKode)) {
			return NOTIFICATION_FOR_ANNET;
		} else {
			return NOTIFICATION_FOR_VEDTAK_VIKTIG_ELLER_IKKE_SATT;
		}
	}

	private static String generateNotificationContentFor(HentForsendelseResponse forsendelse) {
		var nullSafeDistribusjonsTypeKode = forsendelse.distribusjonstype() == null ? VIKTIG : forsendelse.distribusjonstype();
		var mottaker = forsendelse.mottaker();
		return switch (nullSafeDistribusjonsTypeKode) {
			case VEDTAK -> NOTIFICATION_TEXT_FORMAT.formatted(mottaker.mottakerId(),
					mottaker.mottakerNavn(),
					"vedtaket",
					forsendelse.forsendelseTittel(),
					"vedtaket", mottaker.mottakerNavn());
			case VIKTIG -> NOTIFICATION_TEXT_FORMAT.formatted(mottaker.mottakerId(),
					mottaker.mottakerNavn(),
					"viktig brev",
					forsendelse.forsendelseTittel(),
					"brevet", mottaker.mottakerNavn());
			case ANNET -> NOTIFICATION_TEXT_FORMAT.formatted(mottaker.mottakerId(),
					mottaker.mottakerNavn(),
					"meldingen",
					forsendelse.forsendelseTittel(),
					"meldingen", mottaker.mottakerNavn());
		};
	}
}
