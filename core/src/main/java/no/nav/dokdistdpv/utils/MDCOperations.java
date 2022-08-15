package no.nav.dokdistdpv.utils;

import org.apache.camel.Exchange;
import org.slf4j.MDC;

import java.util.UUID;

import static no.nav.dokdistdpv.utils.DokdistdpvConstant.CALL_ID;
import static no.nav.dokdistdpv.utils.DokdistdpvConstant.NAV_CONSUMER_ID;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class MDCOperations {

	public static void setOrGenerateCallId(Exchange exchange) {
		var callId = exchange.getIn().getHeader(CALL_ID, String.class);

		if (isBlank(callId)) {
			callId = UUID.randomUUID().toString();
			exchange.getIn().setHeader(CALL_ID, callId);
		}

		MDC.put(CALL_ID, callId);
	}

	public static void setConsumerId(Exchange exchange) {
		var consumerId = exchange.getIn().getHeader(NAV_CONSUMER_ID, String.class);

		if (!isBlank(consumerId)) {
			MDC.put(NAV_CONSUMER_ID, consumerId);
		}
	}

	public static void removeCallId() {
		MDC.remove(CALL_ID);
	}

	public static void removeConsumerId() {
		MDC.remove(NAV_CONSUMER_ID);
	}

	public static String getCallId() {
		return MDC.get(CALL_ID);
	}
}
