package no.nav.dokdistdpv.kdist003;

import no.altinn.event.domain.CloudEvent;
import no.nav.dokdistdpv.kdist003.domain.InternAltinnHendelse;

import java.util.UUID;

import static no.nav.dokdistdpv.kdist003.CloudEventExtensions.getExtension;

public class MapInternAltinnEvent {

	public static InternAltinnHendelse map(CloudEvent cloudEvent) {
		return InternAltinnHendelse.builder()
				.resource(getExtension(cloudEvent, "resource"))
				.resourceinstance(UUID.fromString(getExtension(cloudEvent, "resourceinstance")))
				.type(cloudEvent.getType())
				.time(cloudEvent.getTime())
				.build();
	}

	private MapInternAltinnEvent() {
	}
}
