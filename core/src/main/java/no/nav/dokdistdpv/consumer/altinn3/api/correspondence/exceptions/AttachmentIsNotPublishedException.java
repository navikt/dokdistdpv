package no.nav.dokdistdpv.consumer.altinn3.api.correspondence.exceptions;

import no.nav.dokdistdpv.exception.DokdistdpvTechnicalException;

public class AttachmentIsNotPublishedException extends DokdistdpvTechnicalException {
	public AttachmentIsNotPublishedException(String message) {
		super(message);
	}
}
