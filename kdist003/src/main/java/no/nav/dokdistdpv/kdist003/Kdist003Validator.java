package no.nav.dokdistdpv.kdist003;

import no.nav.dokdigdirhendelser.altinn.AltinnEvents;

import static java.lang.String.format;
import static no.nav.dokdistdpv.kdist003.Kdist003Constants.ALTERNATIVE_SUBJECT;
import static no.nav.dokdistdpv.kdist003.Kdist003Constants.RESOURCE;
import static no.nav.dokdistdpv.kdist003.domain.AltinnEventMelding.isResourceNavTaushetsbelagtpost;
import static no.nav.dokdistdpv.kdist003.domain.AltinnEventMelding.isSubjectNavOrganisasjon;

public class Kdist003Validator {


	public static void validateAltinnEvent(AltinnEvents altinnEvents) {

		if (altinnEvents.resourceinstance() == null) {
			throw new IllegalArgumentException("resourceinstance kan ikke være null");
		}

		if (!isResourceNavTaushetsbelagtpost(altinnEvents.resource())) {
			throw new IllegalArgumentException(format("Ugyldig verdi: resource %s er ikke lik med %s", altinnEvents.resource(), RESOURCE));

		}

		if (!isSubjectNavOrganisasjon(altinnEvents.alternativesubject())) {
			throw new IllegalArgumentException(format("Ugyldig verdi %s: alternativesubject må være %s", altinnEvents.alternativesubject(), ALTERNATIVE_SUBJECT));

		}

		if (altinnEvents.type() == null) {
			throw new IllegalArgumentException("type kan ikke være null");
		}

		if (altinnEvents.time() == null) {
			throw new IllegalArgumentException("time kan ikke være null");
		}
	}

	private Kdist003Validator() {
	}
}
