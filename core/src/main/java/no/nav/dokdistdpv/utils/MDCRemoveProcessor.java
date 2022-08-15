package no.nav.dokdistdpv.utils;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import static no.nav.dokdistdpv.utils.MDCOperations.removeCallId;
import static no.nav.dokdistdpv.utils.MDCOperations.removeConsumerId;

public class MDCRemoveProcessor implements Processor {
	@Override
	public void process(Exchange exchange) {
		removeCallId();
		removeConsumerId();
	}
}
