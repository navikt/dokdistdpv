package no.nav.dokdistdpv.consumer.altinn3.api.correspondence;

import lombok.Builder;

/**
 * <a href="https://docs.altinn.studio/nb/api/correspondence/spec/#/">Altinn.Correspondence.API</a>
 * <p>
 * Represents the content of a Correspondence.
 */
@Builder
public record InitializeCorrespondenceContentExt(
		String language,
		String messageTitle,
		String messageSummary,
		String messageBody
) {
}