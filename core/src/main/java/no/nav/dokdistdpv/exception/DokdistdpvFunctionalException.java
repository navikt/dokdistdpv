package no.nav.dokdistdpv.exception;

public abstract class DokdistdpvFunctionalException extends RuntimeException{

	public DokdistdpvFunctionalException(String message) {
		super(message);
	}

	public DokdistdpvFunctionalException(String message, Throwable cause) {
		super(message, cause);
	}
}
