package no.nav.dokdistdpv.kdist003;

import no.nav.dokdigdirhendelser.altinn.AltinnEvent;

import static java.lang.String.format;
import static no.nav.dokdistdpv.kdist003.Kdist003Constants.RESOURCE;
import static no.nav.dokdistdpv.kdist003.domain.InternAltinnHendelse.isResourceNavTaushetsbelagtpost;

public class Kdist003Validator {


	public static void validateAltinnEvent(AltinnEvent altinnEvent) {
		if (altinnEvent == null) {
			throw new IllegalArgumentException("altinnEvent kan ikke være null");
		}

		if (altinnEvent.resourceinstance() == null) {
			throw new IllegalArgumentException("resourceinstance kan ikke være null");
		}

		if (!isResourceNavTaushetsbelagtpost(altinnEvent.resource())) {
			throw new IllegalArgumentException(format("Ugyldig verdi: resource %s er ikke lik med %s", altinnEvent.resource(), RESOURCE));

		}

		if (altinnEvent.type() == null) {
			throw new IllegalArgumentException("type kan ikke være null");
		}

		if (altinnEvent.time() == null) {
			throw new IllegalArgumentException("time kan ikke være null");
		}
	}

	private Kdist003Validator() {
	}
}
