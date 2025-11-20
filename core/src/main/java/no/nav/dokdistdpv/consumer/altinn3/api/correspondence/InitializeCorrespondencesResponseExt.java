package no.nav.dokdistdpv.consumer.altinn3.api.correspondence;

import java.util.List;
import java.util.UUID;

public record InitializeCorrespondencesResponseExt(
    List<InitializedCorrespondencesExt> correspondences,
    List<UUID> attachmentIds
) {
	public String getCorrespondenceId() {
		return correspondences.getFirst().correspondenceId().toString();
	}
}