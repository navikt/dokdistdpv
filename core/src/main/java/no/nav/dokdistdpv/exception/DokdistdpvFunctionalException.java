package no.nav.dokdistdpv.exception;

/**
 * Abstract klasse for alle funksjonelle feil i dokdistdpv.
 * Exceptions som arver fra denne klassen trigger typisk:
 * * Melding som havner på funk_feil kø
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
public abstract class DokdistdpvFunctionalException extends RuntimeException{

	public DokdistdpvFunctionalException(String message) {
		super(message);
	}

	public DokdistdpvFunctionalException(Throwable cause) {
		super(cause);
	}

	public DokdistdpvFunctionalException(String message, Throwable cause) {
		super(message, cause);
	}
}
