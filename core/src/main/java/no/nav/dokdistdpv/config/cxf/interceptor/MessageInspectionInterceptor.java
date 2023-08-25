package no.nav.dokdistdpv.config.cxf.interceptor;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;

public class MessageInspectionInterceptor extends AbstractPhaseInterceptor<Message> {

	private static final Logger secureLog = LoggerFactory.getLogger("secureLog");
	public MessageInspectionInterceptor(String phase) {
		super(phase);
	}

	@Override
	public void handleMessage(Message message) throws Fault {
		try {
			Map<String, List<String>> headers = (Map<String, List<String>>) message.get(Message.PROTOCOL_HEADERS);
			secureLog.info("inspectMessage " + prettyPrintHeaders(headers));
			secureLog.info("inspectMessage " + message.getContent(String.class));
		} catch (Exception e) {
			secureLog.warn("inspectMessage tried to inspect message but failed because {}", e.getMessage(), e);
		}

	}

	private static String prettyPrintHeaders(Map<String, List<String>> headers) {
		return headers.entrySet().stream()
				.map(entry ->
						entry.getKey() + ": " + String.join(";", entry.getValue()))
				.collect(joining("\n"));
	}
}
