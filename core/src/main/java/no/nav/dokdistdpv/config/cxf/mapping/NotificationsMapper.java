package no.nav.dokdistdpv.config.cxf.mapping;

import no.altinn.correspondenceagencyexternalaec.Notification;
import no.altinn.correspondenceagencyexternalaec.NotificationBEList;
import no.altinn.correspondenceagencyexternalaec.ReceiverEndPoint;
import no.altinn.correspondenceagencyexternalaec.ReceiverEndPointBEList;
import no.nav.dokdistdpv.consumer.rdist001.domain.DistribusjonsTypeKode;

import static no.altinn.correspondenceagencyexternalaec.TransportType.EMAIL_PREFERRED;
import static no.nav.dokdistdpv.config.cxf.mapping.AltinnForsendelseMapper.FROM_ADDRESS;
import static no.nav.dokdistdpv.config.cxf.mapping.AltinnForsendelseMapper.LANGUAGE_CODE_BOKMAAL;
import static no.nav.dokdistdpv.consumer.rdist001.domain.DistribusjonsTypeKode.ANNET;

public class NotificationsMapper {

	public final static String NOTIFICATION_FOR_ANNET = "VarselDPVUtenRevarsel";
	public final static String NOTIFICATION_FOR_VEDTAK_VIKTIG_ELLER_IKKE_SATT = "VarselDPVMedRevarsel";

	public static NotificationBEList mapNotifications(DistribusjonsTypeKode distribusjonsTypeKode) {
		NotificationBEList notificationList = new NotificationBEList();

		Notification notification = new Notification();
		notification.setFromAddress(FROM_ADDRESS);
		notification.setLanguageCode(LANGUAGE_CODE_BOKMAAL);
		notification.setNotificationType(mapNotificationType(distribusjonsTypeKode));
		notification.setReceiverEndPoints(mapReceiverEndpoints());
		notificationList.getNotification().add(notification);

		return notificationList;
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
}
