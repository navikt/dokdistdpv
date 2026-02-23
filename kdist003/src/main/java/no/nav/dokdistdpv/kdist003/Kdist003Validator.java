package no.nav.dokdistdpv.kdist003;

import no.nav.dokdistdpv.kdist003.domain.AltinnEventMelding;
import org.springframework.validation.annotation.Validated;

import static no.nav.dokdistdpv.kdist003.domain.AltinnEventMelding.isResourceNavTaushetsbelagtpost;
import static no.nav.dokdistdpv.kdist003.domain.AltinnEventMelding.isSubjectNavOrganisasjon;

public class Kdist003Validator {


	public static void validateAltinnEvent(@Validated AltinnEventMelding altinnEventMelding) {

		if (altinnEventMelding.resourceinstance() == null) {
			throw new IllegalArgumentException("resourceinstance kan ikke være null");
		}

		if (!isResourceNavTaushetsbelagtpost(altinnEventMelding.resource())) {
			throw new IllegalArgumentException("Ugyldig verdi: resource er ikke lik " + altinnEventMelding.resource());
		}

		if (!isSubjectNavOrganisasjon(altinnEventMelding.alternativesubject())) {
			throw new IllegalArgumentException("Ugyldig verdi: alternativesubject må være " + altinnEventMelding.alternativesubject());
		}

		if (altinnEventMelding.type() == null) {
			throw new IllegalArgumentException("type kan ikke være null");
		}

		if (altinnEventMelding.time() == null) {
			throw new IllegalArgumentException("time kan ikke være null");
		}
	}

	private Kdist003Validator() {
	}
}
