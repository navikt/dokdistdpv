package no.nav.dokdistdpv.consumer.altinn3.api.correspondence;

import static no.nav.dokdistdpv.consumer.altinn3.Altinn3Constants.NAV_RESOURCE_ID;

/**
 * <a href="https://docs.altinn.studio/nb/api/correspondence/spec/#/">Altinn.Correspondence.API</a>
 * <p>
 * Represents a container object for attachments used when initiating a shared attachment
 */
public record InitializeAttachmentExt(String fileName,
									  String displayName,
									  boolean isEncrypted,
									  String checksum,
									  String sendersReference,
									  String resourceId) {
	public InitializeAttachmentExt {
		if (fileName == null || fileName.length() > 256) {
			throw new IllegalArgumentException("fileName kan ikke være null eller lengre enn 256 tegn");
		}
		if (displayName == null || displayName.length() > 256) {
			throw new IllegalArgumentException("displayName kan ikke være null eller lengre enn 256 tegn");
		}
		if (isEncrypted) {
			throw new IllegalArgumentException("isEncrypted skal være false");
		}
		if (checksum == null || checksum.isBlank()) {
			throw new IllegalArgumentException("checksum kan ikke være null eller blank");
		}
		if (sendersReference == null || sendersReference.isBlank()) {
			throw new IllegalArgumentException("sendersReference kan ikke være null eller blank");
		}
		if (!NAV_RESOURCE_ID.equals(resourceId)) {
			throw new IllegalArgumentException("resourceId må være " + NAV_RESOURCE_ID);
		}
	}
}
