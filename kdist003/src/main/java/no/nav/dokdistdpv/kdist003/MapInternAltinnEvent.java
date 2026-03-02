package no.nav.dokdistdpv.kdist003;

import no.nav.dokdigdirhendelser.altinn.AltinnEvent;
import no.nav.dokdistdpv.kdist003.domain.InternAltinnHendelse;

public class MapInternAltinnEvent {

	public static InternAltinnHendelse map(AltinnEvent altinnEvent) {
		return InternAltinnHendelse.builder()
				.resource(altinnEvent.resource())
				.resourceinstance(altinnEvent.resourceinstance())
				.type(altinnEvent.type())
				.alternativesubject(altinnEvent.alternativesubject())
				.time(altinnEvent.time())
				.build();
	}
}
