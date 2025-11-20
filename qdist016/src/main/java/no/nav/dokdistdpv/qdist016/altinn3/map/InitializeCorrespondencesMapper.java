package no.nav.dokdistdpv.qdist016.altinn3.map;

import no.nav.dokdistdpv.consumer.altinn3.api.correspondence.BaseCorrespondenceExt;
import no.nav.dokdistdpv.consumer.altinn3.api.correspondence.InitializeCorrespondenceContentExt;
import no.nav.dokdistdpv.consumer.altinn3.api.correspondence.InitializeCorrespondenceNotificationExt;
import no.nav.dokdistdpv.consumer.altinn3.api.correspondence.InitializeCorrespondencesExt;
import no.nav.dokdistdpv.consumer.rdist001.domain.DistribusjonsTypeKode;
import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse;
import no.nav.dokdistdpv.qdist016.altinn3.UploadedAttachment;

import java.util.List;
import java.util.UUID;

import static no.nav.dokdistdpv.consumer.altinn3.Altinn3Constants.NAV_RESOURCE_ID;
import static no.nav.dokdistdpv.consumer.altinn3.api.correspondence.EmailContentType.Plain;
import static no.nav.dokdistdpv.consumer.altinn3.api.correspondence.NotificationChannelExt.EmailPreferred;
import static no.nav.dokdistdpv.consumer.altinn3.api.correspondence.NotificationTemplateExt.CustomMessage;
import static no.nav.dokdistdpv.qdist016.altinn3.map.ContentNotificationTekst.annet;
import static no.nav.dokdistdpv.qdist016.altinn3.map.ContentNotificationTekst.vedtak;
import static no.nav.dokdistdpv.qdist016.altinn3.map.ContentNotificationTekst.viktig;

public class InitializeCorrespondencesMapper {
	static final String ISO_IEC_6523_ICD_ORGANISASJONSNUMMER = "0192";
	static final String ISO_639_1_NORSK_BOKMAAL = "nb";

	public static InitializeCorrespondencesExt map(HentForsendelseResponse forsendelse, List<UploadedAttachment> uploadedAttachments) {
		ContentNotificationTekst tekst = mapTekst(forsendelse.distribusjonstype(), forsendelse.forsendelseTittel());
		return new InitializeCorrespondencesExt(
				mapCorrespondence(forsendelse, tekst),
				mapRecipients(forsendelse),
				mapExistingAttachments(uploadedAttachments),
				UUID.fromString(forsendelse.bestillingsId())
		);
	}

	private static ContentNotificationTekst mapTekst(DistribusjonsTypeKode distribusjonstype, String forsendelseTittel) {
		if (distribusjonstype == null) {
			return viktig(forsendelseTittel);
		}
		return switch (distribusjonstype) {
			case VEDTAK -> vedtak(forsendelseTittel);
			case VIKTIG -> viktig(forsendelseTittel);
			case ANNET -> annet(forsendelseTittel);
		};
	}

	private static BaseCorrespondenceExt mapCorrespondence(HentForsendelseResponse forsendelse, ContentNotificationTekst tekst) {
		return BaseCorrespondenceExt.builder()
				.resourceId(NAV_RESOURCE_ID)
				.sendersReference(forsendelse.bestillingsId())
				.content(mapCorrespondenceContent(tekst))
				.notification(mapCorrespondenceNotification(tekst))
				.isConfidential(true)
				.build();
	}

	private static InitializeCorrespondenceContentExt mapCorrespondenceContent(ContentNotificationTekst tekst) {
		return InitializeCorrespondenceContentExt.builder()
				.language(ISO_639_1_NORSK_BOKMAAL)
				.messageTitle(tekst.messageTitle())
				.messageSummary(tekst.messageSummary())
				.messageBody(tekst.messageBody())
				.build();
	}

	private static InitializeCorrespondenceNotificationExt mapCorrespondenceNotification(ContentNotificationTekst tekst) {
		return InitializeCorrespondenceNotificationExt.builder()
				.notificationTemplate(CustomMessage)
				.emailSubject(tekst.emailSubject())
				.emailBody(tekst.emailBody())
				.emailContentType(Plain)
				.smsBody(tekst.smsBody())
				.sendReminder(true)
				.reminderEmailSubject(tekst.reminderEmailSubject())
				.reminderEmailBody(tekst.reminderEmailBody())
				.reminderEmailContentType(Plain)
				.reminderSmsBody(tekst.reminderSmsBody())
				.notificationChannel(EmailPreferred)
				.reminderNotificationChannel(EmailPreferred)
				.build();
	}

	private static List<String> mapRecipients(HentForsendelseResponse forsendelse) {
		return List.of(ISO_IEC_6523_ICD_ORGANISASJONSNUMMER + ":" + forsendelse.mottaker().mottakerId());
	}

	private static List<UUID> mapExistingAttachments(List<UploadedAttachment> attachmentIds) {
		return attachmentIds.stream()
				.sorted()
				.map(uploadedAttachment -> UUID.fromString(uploadedAttachment.attachmentId()))
				.toList();
	}
}
