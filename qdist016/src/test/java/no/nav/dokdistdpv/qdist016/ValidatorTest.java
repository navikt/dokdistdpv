package no.nav.dokdistdpv.qdist016;

import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse;
import no.nav.dokdistdpv.exception.ForsendelseValidationException;
import org.junit.jupiter.api.Test;

import static no.nav.dokdistdpv.qdist016.Validator.validerForsendelse;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidatorTest {

	private static final String FORSENDELSE_ID = "FORSENDELSE_ID";

	@Test
	void shouldPassIfStatusIsKlarForDist() {
		String status = "KLAR_FOR_DIST";
		HentForsendelseResponse forsendelse = createHentForsendelseResponse(status);

		assertDoesNotThrow(() -> validerForsendelse(FORSENDELSE_ID, forsendelse));
	}

	@Test
	void shouldThrowExceptionIfStatusIsNotKlarForDist() {
		String status = "OPPRETTET";
		HentForsendelseResponse forsendelse = createHentForsendelseResponse(status);

		Exception e = assertThrows(ForsendelseValidationException.class, () -> validerForsendelse(FORSENDELSE_ID, forsendelse));
		assertEquals(e.getMessage(), "Validering av forsendelse feilet for forsendelse med id=" + FORSENDELSE_ID + " og status=" + status);
	}

	private HentForsendelseResponse createHentForsendelseResponse(String status) {
		return new HentForsendelseResponse(
				null,
				null,
				null,
				null,
				status,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null
		);
	}

}