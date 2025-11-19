package no.nav.dokdistdpv.exception;

public class DokdistdpvTechnicalException extends RuntimeException {

	public DokdistdpvTechnicalException(String message) {
		super(message);
	}

	public DokdistdpvTechnicalException(String message, Throwable cause) {
		super(message, cause);
	}
}
