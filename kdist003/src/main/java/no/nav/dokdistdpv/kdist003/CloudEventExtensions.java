package no.nav.dokdistdpv.kdist003;

import no.altinn.event.domain.CloudEvent;

/**
 * Utility for accessing CloudEvent extension attributes per the CNCF CloudEvents specification.
 * Extension attributes are stored in extensionAttributes list with their value in type.name.
 */
public final class CloudEventExtensions {

	private CloudEventExtensions() {
	}

	/**
	 * Get extension attribute value by name from a CloudEvent.
	 */
	public static String getExtension(CloudEvent cloudEvent, String extensionName) {
		if (cloudEvent == null || cloudEvent.getExtensionAttributes() == null) {
			return null;
		}
		return cloudEvent.getExtensionAttributes().stream()
				.filter(attr -> extensionName.equals(attr.getName()))
				.map(attr -> attr.getType() != null ? attr.getType().getName() : null)
				.findFirst()
				.orElse(null);
	}
}
