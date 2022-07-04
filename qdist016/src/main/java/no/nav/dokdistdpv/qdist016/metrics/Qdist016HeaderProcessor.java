package no.nav.dokdistdpv.qdist016.metrics;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.language.xpath.XPathBuilder;

import static no.nav.dokdistdpv.utils.DokdistdpvConstant.PROPERTY_FORSENDELSE_ID;

public class Qdist016HeaderProcessor implements Processor {
	@Override
	public void process(Exchange exchange) {
		//TODO sett callId and stuff her
		setForsendelseIdAsProperty(exchange);
	}

	private void setForsendelseIdAsProperty(Exchange exchange) {
		String forsendelseId = XPathBuilder.xpath("//forsendelseId/text()").evaluate(exchange, String.class);
		if (forsendelseId == null || forsendelseId.trim().isEmpty()) {
			return;
		}
		exchange.setProperty(PROPERTY_FORSENDELSE_ID, forsendelseId);
	}
}
