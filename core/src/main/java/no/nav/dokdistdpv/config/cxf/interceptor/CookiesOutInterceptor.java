package no.nav.dokdistdpv.config.cxf.interceptor;


import no.nav.dokdistdpv.config.cxf.cookies.CookieStore;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.apache.cxf.message.Message.PROTOCOL_HEADERS;
import static org.apache.cxf.phase.Phase.PRE_PROTOCOL;

/**
 * Interceptor for å hente <i>Cookie</i> fra {@link CookieStore} og legge til i header i utgående webservice melding.
 */
public class CookiesOutInterceptor extends AbstractPhaseInterceptor<Message> {

    public CookiesOutInterceptor() {
        super(PRE_PROTOCOL);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        Map<String, List> headers = (Map<String, List>) message.get(PROTOCOL_HEADERS);
        if (CookieStore.getCookie() != null) {
            headers.put("Cookie", Collections.singletonList(CookieStore.getCookie()));
        }
    }

}
