package no.nav.dokdistdpv.exception;

public abstract class DokdistdpvTechnicalException extends RuntimeException {

	public DokdistdpvTechnicalException(String message, Throwable cause) {
		super(message, cause);
	}
}
