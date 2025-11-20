package no.nav.dokdistdpv.consumer.altinn3.api.correspondence;

import java.util.List;
import java.util.UUID;

public record InitializedCorrespondencesExt(
    UUID correspondenceId,
    String status,
    String recipient,
    List<InitializedCorrespondencesNotificationsExt> notifications
) {
	@Override
	public String toString() {
		return "Correspondence(" +
				"correspondenceId=" + correspondenceId +
				", status='" + status + '\'' +
				", notifications=" + notifications +
				')';
	}
}