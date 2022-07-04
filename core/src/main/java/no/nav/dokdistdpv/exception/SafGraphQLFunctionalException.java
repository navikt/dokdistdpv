package no.nav.dokdistdpv.exception;

public class SafGraphQLFunctionalException extends DokdistdpvFunctionalException{
	public SafGraphQLFunctionalException(String message, Throwable cause) {
		super(message, cause);
	}

	public SafGraphQLFunctionalException(String message) {
		super(message);
	}
}
