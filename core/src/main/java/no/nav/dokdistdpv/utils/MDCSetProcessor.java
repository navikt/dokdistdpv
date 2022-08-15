package no.nav.dokdistdpv.utils;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import static no.nav.dokdistdpv.utils.MDCOperations.setConsumerId;
import static no.nav.dokdistdpv.utils.MDCOperations.setOrGenerateCallId;

public class MDCSetProcessor implements Processor {
	@Override
	public void process(Exchange exchange) {
		setOrGenerateCallId(exchange);
		setConsumerId(exchange);
	}
}
