package no.nav.dokdistdpv.kdist003;

import no.nav.dokdistdpv.kdist003.domain.InternAltinnEvents;

import static java.lang.String.format;
import static no.nav.dokdistdpv.kdist003.Kdist003Constants.ALTERNATIVE_SUBJECT;
import static no.nav.dokdistdpv.kdist003.Kdist003Constants.RESOURCE;
import static no.nav.dokdistdpv.kdist003.domain.InternAltinnEvents.isResourceNavTaushetsbelagtpost;
import static no.nav.dokdistdpv.kdist003.domain.InternAltinnEvents.isSubjectNavOrganisasjon;

public class Kdist003Validator {


	public static void validateAltinnEvent(InternAltinnEvents internAltinnEvents) {

		if (internAltinnEvents.resourceinstance() == null) {
			throw new IllegalArgumentException("resourceinstance kan ikke være null");
		}

		if (!isResourceNavTaushetsbelagtpost(internAltinnEvents.resource())) {
			throw new IllegalArgumentException(format("Ugyldig verdi: resource %s er ikke lik med %s", internAltinnEvents.resource(), RESOURCE));

		}

		if (!isSubjectNavOrganisasjon(internAltinnEvents.alternativesubject())) {
			throw new IllegalArgumentException(format("Ugyldig verdi %s: alternativesubject må være %s", internAltinnEvents.alternativesubject(), ALTERNATIVE_SUBJECT));

		}

		if (internAltinnEvents.type() == null) {
			throw new IllegalArgumentException("type kan ikke være null");
		}

		if (internAltinnEvents.time() == null) {
			throw new IllegalArgumentException("time kan ikke være null");
		}
	}

	private Kdist003Validator() {
	}
}
