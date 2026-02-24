package no.nav.dokdistdpv.kdist003;

import no.nav.dokdigdirhendelser.altinn.AltinnEvents;
import no.nav.dokdistdpv.kdist003.domain.InternAltinnEvents;

public class MapInternAltinnEvent {

	public static InternAltinnEvents map(AltinnEvents altinnEvents) {
		return InternAltinnEvents.builder()
				.id(altinnEvents.id())
				.resource(altinnEvents.resource())
				.resourceinstance(altinnEvents.resourceinstance())
				.type(altinnEvents.type())
				.source(altinnEvents.source())
				.specversion(altinnEvents.specversion())
				.alternativesubject(altinnEvents.alternativesubject())
				.time(altinnEvents.time())
				.build();
	}
}
