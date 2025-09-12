package no.nav.dokdistdpv.config.cxf.interceptor;

import no.nav.dokdistdpv.config.cxf.cookies.CookieStore;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.Cookie;

import java.util.List;
import java.util.Map;

/**
 * Interceptor for å plukke opp <i>Cookie</i> fra header i innkommende webservice melding.
 * Den vil da finnes som en attributt <i>Set-Cookie</i>. Hvis funnet så lagres den i {@link CookieStore}.
 */
public class CookiesInInterceptor extends AbstractPhaseInterceptor<Message> {

	public CookiesInInterceptor() {
		super(Phase.PRE_PROTOCOL);
	}

	@Override
	public void handleMessage(Message message) throws Fault {
		Map<String, List> headers = (Map<String, List>) message.get(Message.PROTOCOL_HEADERS);
		List<Cookie> cookies = headers.get("Set-Cookie");
		if (cookies != null) {
			CookieStore.setCookie(cookies.getFirst());
		}
	}

}
