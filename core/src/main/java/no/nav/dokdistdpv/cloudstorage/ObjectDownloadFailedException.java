package no.nav.dokdistdpv.cloudstorage;

import no.nav.dokdistdpv.exception.DokdistdpvTechnicalException;

/**
 * Kastes hvis nedlasting eller dekryptering av objekt feiler.
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
public class ObjectDownloadFailedException extends DokdistdpvTechnicalException {
	public ObjectDownloadFailedException(String message) {
		super(message);
	}

	public ObjectDownloadFailedException(String message, Throwable cause) {
		super(message, cause);
	}
}
