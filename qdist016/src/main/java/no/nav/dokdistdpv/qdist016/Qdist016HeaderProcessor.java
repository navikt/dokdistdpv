package no.nav.dokdistdpv.qdist016;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.language.xpath.XPathBuilder;

import static no.nav.dokdistdpv.utils.DokdistdpvConstant.PROPERTY_FORSENDELSE_ID;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class Qdist016HeaderProcessor implements Processor {
	@Override
	public void process(Exchange exchange) {
		setForsendelseIdAsProperty(exchange);
	}

	private void setForsendelseIdAsProperty(Exchange exchange) {
		var forsendelseId = XPathBuilder.xpath("//forsendelseId/text()").evaluate(exchange, String.class);

		if (isBlank(forsendelseId)) {
			return;
		}

		exchange.setProperty(PROPERTY_FORSENDELSE_ID, forsendelseId);
	}
}
