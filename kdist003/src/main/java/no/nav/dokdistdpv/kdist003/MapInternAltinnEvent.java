package no.nav.dokdistdpv.kdist003;

import no.altinn.event.domain.CloudEvent;
import no.nav.dokdistdpv.kdist003.domain.InternAltinnHendelse;

public class MapInternAltinnEvent {

	public static InternAltinnHendelse map(CloudEvent cloudEvent) {
		return InternAltinnHendelse.builder()
				.resource(cloudEvent.getResource())
				.resourceinstance(cloudEvent.getResourceinstance())
				.type(cloudEvent.getType())
				.time(cloudEvent.getTime())
				.build();
	}

	private MapInternAltinnEvent() {
	}
}
