package no.nav.dokdistdpv.config.cxf.interceptor;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpv.config.cxf.cookies.CookieStore;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.interceptor.Soap12FaultInInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.ws.security.tokenstore.TokenStoreException;

import javax.xml.namespace.QName;
import java.util.List;

import static org.apache.cxf.phase.Phase.UNMARSHAL;
import static org.apache.cxf.ws.security.SecurityConstants.TOKEN;
import static org.apache.cxf.ws.security.SecurityConstants.TOKEN_ID;
import static org.apache.cxf.ws.security.tokenstore.TokenStoreUtils.getTokenStore;

/**
 * Interceptor for å håndtere feil med context token.
 */
@Slf4j
public class BadContextTokenInFaultInterceptor extends AbstractPhaseInterceptor<Message> {

    private static final String ERROR_CODE_BAD_CONTEXT_TOKEN = "BadContextToken";

    public BadContextTokenInFaultInterceptor() {
        super(UNMARSHAL);
        getAfter().add(Soap12FaultInInterceptor.class.getName());
    }

    @Override
    public void handleMessage(Message message) {
        Exception exception = message.getContent(Exception.class);
        if (exception instanceof SoapFault soapFault) {
            log.error("Server Gods not happy, sent you a soapFault.. Trying to recover..");
            List<QName> subCodes = soapFault.getSubCodes();
            for (QName subCode : subCodes) {
                log.error("Found subCode: " + subCode.getLocalPart());
                if (subCode.getLocalPart().equalsIgnoreCase(ERROR_CODE_BAD_CONTEXT_TOKEN)) {
                    String tokenId = (String)message.getContextualProperty(TOKEN_ID);

                    // TODO: Betre handtering
                    try {
                        removeTokenFromMessageAndTokenStore(message, tokenId);
                    } catch (TokenStoreException e) {
                        e.printStackTrace();
                    }
                    CookieStore.setCookie(null);
                    soapFault.setMessage("Token " + tokenId + " is removed from tokenstore, a new one will be requested on your next call. Message from server: " + soapFault.getMessage());
                    message.setContent(Exception.class, soapFault);
                }
            }
        }
    }

    private void removeTokenFromMessageAndTokenStore(Message message, String tokenId) throws TokenStoreException {
        message.getExchange().getEndpoint().remove(TOKEN);
        message.getExchange().getEndpoint().remove(TOKEN_ID);
        message.getExchange().remove(TOKEN_ID);
        message.getExchange().remove(TOKEN);
        getTokenStore(message).remove(tokenId);
        log.error("Removed token " + tokenId + " from message and tokenStore");
    }

}
