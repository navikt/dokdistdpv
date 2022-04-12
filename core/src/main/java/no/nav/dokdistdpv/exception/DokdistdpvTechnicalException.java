package no.nav.dokdistdpv.exception;

/**
 * Abstract klasse for alle tekniske feil i dokdistdpv.
 * Exceptions som arver fra denne klassen trigger typisk:
 * * Nye tekniske forsøk (retry)
 * * Skriving til backout kø (BQ)
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
public abstract class DokdistdpvTechnicalException extends RuntimeException {

	public DokdistdpvTechnicalException(String message) {
		super(message);
	}

	public DokdistdpvTechnicalException(String message, Throwable cause) {
		super(message, cause);
	}
}
