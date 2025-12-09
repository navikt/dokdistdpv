package no.nav.dokdistdpv.consumer.altinn3.api.correspondence;

import java.util.UUID;

public record InitializedCorrespondencesNotificationsExt(
		UUID orderId,
		boolean isReminder,
		String status
) {
	@Override
	public String toString() {
		return "Notification(" +
				"orderId=" + orderId +
				", status='" + status + '\'' +
				')';
	}
}