package no.nav.dokdistdpv.kdist003;

import no.altinn.event.domain.CloudEvent;

import static java.lang.String.format;
import static no.nav.dokdistdpv.kdist003.CloudEventExtensions.getExtension;
import static no.nav.dokdistdpv.kdist003.Kdist003Constants.RESOURCE;
import static no.nav.dokdistdpv.kdist003.domain.InternAltinnHendelse.isResourceNavTaushetsbelagtpost;

public class Kdist003Validator {


	public static void validateCloudEvent(CloudEvent cloudEvent) {
		if (cloudEvent == null) {
			throw new IllegalArgumentException("cloudEvent kan ikke være null");
		}

		String resourceinstance = getExtension(cloudEvent, "resourceinstance");
		if (resourceinstance == null) {
			throw new IllegalArgumentException("resourceinstance kan ikke være null");
		}

		String resource = getExtension(cloudEvent, "resource");
		if (!isResourceNavTaushetsbelagtpost(resource)) {
			throw new IllegalArgumentException(format("Ugyldig verdi: resource %s er ikke lik med %s", resource, RESOURCE));

		}

		if (cloudEvent.getType() == null) {
			throw new IllegalArgumentException("type kan ikke være null");
		}

		if (cloudEvent.getTime() == null) {
			throw new IllegalArgumentException("time kan ikke være null");
		}
	}

	private Kdist003Validator() {
	}
}
