package no.nav.dokdistdpv.qdist016.altinn3.map;

import no.altinn.services.altinn3.openapi.domain.BaseCorrespondenceExt;
import no.altinn.services.altinn3.openapi.domain.InitializeCorrespondenceContentExt;
import no.altinn.services.altinn3.openapi.domain.InitializeCorrespondenceNotificationExt;
import no.altinn.services.altinn3.openapi.domain.InitializeCorrespondencesExt;
import no.nav.dokdistdpv.consumer.rdist001.domain.DistribusjonsTypeKode;
import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse;
import no.nav.dokdistdpv.qdist016.altinn3.UploadedAttachment;

import java.util.List;
import java.util.UUID;

import static no.altinn.services.altinn3.openapi.domain.EmailContentType.PLAIN;
import static no.altinn.services.altinn3.openapi.domain.NotificationChannelExt.EMAIL_PREFERRED;
import static no.altinn.services.altinn3.openapi.domain.NotificationTemplateExt.CUSTOM_MESSAGE;
import static no.nav.dokdistdpv.consumer.altinn3.Altinn3Constants.NAV_RESOURCE_ID;
import static no.nav.dokdistdpv.qdist016.altinn3.map.ContentNotificationTekst.annet;
import static no.nav.dokdistdpv.qdist016.altinn3.map.ContentNotificationTekst.vedtak;
import static no.nav.dokdistdpv.qdist016.altinn3.map.ContentNotificationTekst.viktig;

public class InitializeCorrespondencesMapper {
	static final String ISO_IEC_6523_ICD_ORGANISASJONSNUMMER = "0192";
	static final String ISO_639_1_NORSK_BOKMAAL = "nb";

	public static InitializeCorrespondencesExt map(HentForsendelseResponse forsendelse, List<UploadedAttachment> uploadedAttachments) {
		ContentNotificationTekst tekst = mapTekst(forsendelse.distribusjonstype(), forsendelse.forsendelseTittel());
		return InitializeCorrespondencesExt.builder()
				.correspondence(mapCorrespondence(forsendelse, tekst))
				.recipients(mapRecipients(forsendelse))
				.existingAttachments(mapExistingAttachments(uploadedAttachments))
				.idempotentKey(UUID.fromString(forsendelse.bestillingsId()))
				.build();
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
				.isConfirmationNeeded(false)
				.ignoreReservation(false)
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
				.notificationTemplate(CUSTOM_MESSAGE)
				.emailSubject(tekst.emailSubject())
				.emailBody(tekst.emailBody())
				.emailContentType(PLAIN)
				.smsBody(tekst.smsBody())
				.sendReminder(true)
				.reminderEmailSubject(tekst.reminderEmailSubject())
				.reminderEmailBody(tekst.reminderEmailBody())
				.reminderEmailContentType(PLAIN)
				.reminderSmsBody(tekst.reminderSmsBody())
				.notificationChannel(EMAIL_PREFERRED)
				.reminderNotificationChannel(EMAIL_PREFERRED)
				.overrideRegisteredContactInformation(false)
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
