package no.nav.dokdistdpv.consumer.altinn3.api.correspondence;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

/**
 * <a href="https://docs.altinn.studio/nb/api/correspondence/spec/#/">Altinn.Correspondence.API</a>
 * <p>
 * Represents an overview of a shared attachment that can be used by multiple correspondences
 */
public record AttachmentOverviewExt(
		String fileName,
		String displayName,
		boolean isEncrypted,
		String checksum,
		String sendersReference,
		String resourceId,
		UUID attachmentId,
		String status,
		String statusText,
		ZonedDateTime statusChanged,
		List<UUID> correspondenceIds,
		String dataType
) {
	@Override
	public String toString() {
		return "Attachment(" +
				"sendersReference='" + sendersReference + '\'' +
				", attachmentId=" + attachmentId +
				')';
	}
}