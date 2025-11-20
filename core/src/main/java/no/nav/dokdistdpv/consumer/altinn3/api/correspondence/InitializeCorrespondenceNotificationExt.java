package no.nav.dokdistdpv.consumer.altinn3.api.correspondence;

import lombok.Builder;

/**
 * <a href="https://docs.altinn.studio/nb/api/correspondence/spec/#/">Altinn.Correspondence.API</a>
 * <p>
 * Used to specify a single notification connected to a specific Correspondence during the Initialize Correspondence operation
 */
@Builder
public record InitializeCorrespondenceNotificationExt(
		NotificationTemplateExt notificationTemplate,
		String emailSubject,
		String emailBody,
		EmailContentType emailContentType,
		String smsBody,
		boolean sendReminder,
		String reminderEmailSubject,
		String reminderEmailBody,
		EmailContentType reminderEmailContentType,
		String reminderSmsBody,
		NotificationChannelExt notificationChannel,
		NotificationChannelExt reminderNotificationChannel,
		String sendersReference
) {
}