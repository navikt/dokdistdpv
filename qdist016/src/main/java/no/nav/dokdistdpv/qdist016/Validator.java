package no.nav.dokdistdpv.qdist016;

import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse;
import no.nav.dokdistdpv.exception.ForsendelseValidationException;

import static java.lang.String.format;

public class Validator {

	public static void validerForsendelse(String forsendelseId, HentForsendelseResponse forsendelse) {
		if (!forsendelse.forsendelseStatus().equals("KLAR_FOR_DIST")) {
			throw new ForsendelseValidationException(format("Validering av forsendelse feilet for forsendelse med id=%s og status=%s",
							forsendelseId,
							forsendelse.forsendelseStatus())
			);
		}
	}
}
