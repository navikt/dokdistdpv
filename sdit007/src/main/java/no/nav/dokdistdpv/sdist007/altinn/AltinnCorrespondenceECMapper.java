package no.nav.dokdistdpv.sdist007.altinn;

import no.altinn.correspondenceagencyexternalaec.ExternalContentV2;
import no.altinn.correspondenceagencyexternalaec.InsertCorrespondenceV2;
import no.altinn.correspondenceagencyexternalaec.Notification;
import no.altinn.correspondenceagencyexternalaec.NotificationBEList;
import no.altinn.correspondenceagencyexternalaec.ReceiverEndPoint;
import no.altinn.correspondenceagencyexternalaec.ReceiverEndPointBEList;
import no.altinn.correspondenceagencyexternalaec.TextToken;
import no.altinn.correspondenceagencyexternalaec.TextTokenSubstitutionBEList;
import no.nav.dokdistdpv.consumer.rdist001.domain.DistribusjonsTypeKode;
import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse;
import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse.Mottaker;

import static java.util.Objects.isNull;
import static no.altinn.correspondenceagencyexternalaec.TransportType.EMAIL_PREFERRED;
import static no.nav.dokdistdpv.consumer.rdist001.domain.DistribusjonsTypeKode.ANNET;
import static no.nav.dokdistdpv.sdist007.altinn.AltinnMessage.MESSAGE_BODY;
import static no.nav.dokdistdpv.sdist007.altinn.AltinnMessage.SERVICE_CODE;
import static no.nav.dokdistdpv.sdist007.altinn.AltinnMessage.SERVICE_EDITION_CODE;
import static no.nav.dokdistdpv.sdist007.altinn.AltinnMessage.TOKEN_NUM;
import static no.nav.dokdistdpv.sdist007.altinn.AltinnMessage.TOKEN_VALUE;
import static no.nav.dokdistdpv.utils.AltinnConstant.BREVET;
import static no.nav.dokdistdpv.utils.AltinnConstant.FROM_ADDRESS;
import static no.nav.dokdistdpv.utils.AltinnConstant.LANGUAGE_CODE_BOKMAAL;
import static no.nav.dokdistdpv.utils.AltinnConstant.MELDING;
import static no.nav.dokdistdpv.utils.AltinnConstant.MELDINGEN;
import static no.nav.dokdistdpv.utils.AltinnConstant.MESSAGE_TITLE_ANNET;
import static no.nav.dokdistdpv.utils.AltinnConstant.MESSAGE_TITLE_VEDTAK;
import static no.nav.dokdistdpv.utils.AltinnConstant.MESSAGE_TITLE_VIKTIG;
import static no.nav.dokdistdpv.utils.AltinnConstant.NOTIFICATION_MED_REVARSEL;
import static no.nav.dokdistdpv.utils.AltinnConstant.NOTIFICATION_UTEN_REVARSEL;
import static no.nav.dokdistdpv.utils.AltinnConstant.VEDTAK;
import static no.nav.dokdistdpv.utils.AltinnConstant.VEDTAKET;
import static no.nav.dokdistdpv.utils.AltinnConstant.VIKTIG_BREV;

public class AltinnCorrespondenceECMapper {

	public InsertCorrespondenceV2 mapToCorrespondence(HentForsendelseResponse hentForsendelseResponse) {
		InsertCorrespondenceV2 insertCorrespondenceV2 = new InsertCorrespondenceV2();

		insertCorrespondenceV2.setServiceCode(SERVICE_CODE);
		insertCorrespondenceV2.setServiceEdition(SERVICE_EDITION_CODE);

		insertCorrespondenceV2.setReportee(hentForsendelseResponse.mottaker().mottakerId());
		insertCorrespondenceV2.setContent(mapToExternalContentV2(hentForsendelseResponse));

		NotificationBEList notificationBEList = new NotificationBEList();
		notificationBEList.getNotification().add(mapNotification(hentForsendelseResponse));
		insertCorrespondenceV2.setNotifications(notificationBEList);

		return insertCorrespondenceV2;
	}

	private ExternalContentV2 mapToExternalContentV2(HentForsendelseResponse hentForsendelseResponse) {
		ExternalContentV2 externalContentV2 = new ExternalContentV2();

		externalContentV2.setLanguageCode(LANGUAGE_CODE_BOKMAAL);
		externalContentV2.setMessageTitle(getMessageTitle(hentForsendelseResponse.distribusjonstype()));
		externalContentV2.setMessageBody(MESSAGE_BODY);

		return externalContentV2;
	}

	private Notification mapNotification(HentForsendelseResponse hentForsendelseResponse) {
		Notification notification = new Notification();

		notification.setFromAddress(FROM_ADDRESS);
		notification.setLanguageCode(LANGUAGE_CODE_BOKMAAL);
		notification.setNotificationType(mapNotificationType(hentForsendelseResponse.distribusjonstype()));

		TextTokenSubstitutionBEList tokenSubstitutionBEList = new TextTokenSubstitutionBEList();
		tokenSubstitutionBEList.getTextToken().add(mapTextToken(hentForsendelseResponse));

		notification.setTextTokens(tokenSubstitutionBEList);
		notification.setReceiverEndPoints(mapReceiverEndPoint());

		return notification;
	}

	private TextToken mapTextToken(HentForsendelseResponse hentForsendelse) {
		TextToken textToken = new TextToken();
		textToken.setTokenNum(TOKEN_NUM);
		textToken.setTokenValue(mapTokenValue(hentForsendelse.distribusjonstype(), hentForsendelse.mottaker()));
		return textToken;
	}

	private ReceiverEndPointBEList mapReceiverEndPoint() {
		ReceiverEndPointBEList receiverEndPointBEList = new ReceiverEndPointBEList();
		ReceiverEndPoint receiverEndPoint = new ReceiverEndPoint();
		receiverEndPoint.setTransportType(EMAIL_PREFERRED);

		receiverEndPointBEList.getReceiverEndPoint().add(receiverEndPoint);
		return receiverEndPointBEList;
	}


	private String getMessageTitle(DistribusjonsTypeKode distribusjonsType) {
		if (isNull(distribusjonsType)) {
			return MESSAGE_TITLE_VIKTIG;
		}
		return switch (distribusjonsType) {
			case VEDTAK -> MESSAGE_TITLE_VEDTAK;
			case VIKTIG -> MESSAGE_TITLE_VIKTIG;
			case ANNET -> MESSAGE_TITLE_ANNET;
		};
	}

	private String mapNotificationType(DistribusjonsTypeKode distribusjonsType) {
		if (ANNET.equals(distribusjonsType)) {
			return NOTIFICATION_UTEN_REVARSEL;
		} else {
			return NOTIFICATION_MED_REVARSEL;
		}
	}

	private String mapTokenValue(DistribusjonsTypeKode distribusjonsType, Mottaker mottaker) {
		return switch (distribusjonsType) {
			case VEDTAK -> TOKEN_VALUE.formatted(mottaker.mottakerId(), mottaker.mottakerNavn(),
					VEDTAK, VEDTAKET, VEDTAKET.toLowerCase(), mottaker.mottakerNavn());
			case ANNET -> TOKEN_VALUE.formatted(mottaker.mottakerId(), mottaker.mottakerNavn(),
					MELDING, MELDINGEN, MELDINGEN.toLowerCase(), mottaker.mottakerNavn());
			default -> TOKEN_VALUE.formatted(mottaker.mottakerId(), mottaker.mottakerNavn(),
					VIKTIG_BREV, BREVET, BREVET.toLowerCase(), mottaker.mottakerNavn());
		};
	}

}
