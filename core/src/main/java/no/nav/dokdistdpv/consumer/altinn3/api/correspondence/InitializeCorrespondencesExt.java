package no.nav.dokdistdpv.consumer.altinn3.api.correspondence;

import java.util.List;
import java.util.UUID;

/**
 * <a href="https://docs.altinn.studio/nb/api/correspondence/spec/#/">Altinn.Correspondence.API</a>
 */
public record InitializeCorrespondencesExt(
		BaseCorrespondenceExt correspondence,
		List<String> recipients,
		List<UUID> existingAttachments,
		UUID idempotentKey
) {}