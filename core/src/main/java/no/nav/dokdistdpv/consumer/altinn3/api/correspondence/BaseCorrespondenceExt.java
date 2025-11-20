package no.nav.dokdistdpv.consumer.altinn3.api.correspondence;

import lombok.Builder;

/**
 * <a href="https://docs.altinn.studio/nb/api/correspondence/spec/#/">Altinn.Correspondence.API</a>
 * <p>
 * Represents a request object for the operation, InitializeCorrespondence, that can create a correspondence in Altinn.
 */
@Builder
public record BaseCorrespondenceExt(
		String resourceId,
		String sendersReference,
		String messageSender,
		InitializeCorrespondenceContentExt content,
		InitializeCorrespondenceNotificationExt notification,
		boolean isConfidential
) {
}