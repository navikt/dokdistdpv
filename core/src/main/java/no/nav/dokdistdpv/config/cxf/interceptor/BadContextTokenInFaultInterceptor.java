package no.nav.dokdistdpv.config.cxf.interceptor;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpv.config.cxf.cookies.CookieStore;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.interceptor.Soap12FaultInInterceptor;
import org.apache.cxf.common.security.SecurityToken;
import org.apache.cxf.common.security.UsernameToken;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.ws.security.tokenstore.TokenStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.joining;
import static org.apache.cxf.phase.Phase.UNMARSHAL;
import static org.apache.cxf.ws.security.SecurityConstants.TOKEN;
import static org.apache.cxf.ws.security.SecurityConstants.TOKEN_ID;
import static org.apache.cxf.ws.security.tokenstore.TokenStoreUtils.getTokenStore;

/**
 * Interceptor for å håndtere feil med context token.
 */
@Slf4j
public class BadContextTokenInFaultInterceptor extends AbstractPhaseInterceptor<Message> {

	private static final Logger secureLog = LoggerFactory.getLogger("secureLog");
    private static final String ERROR_CODE_BAD_CONTEXT_TOKEN = "BadContextToken";

    public BadContextTokenInFaultInterceptor() {
        super(UNMARSHAL);
        getAfter().add(Soap12FaultInInterceptor.class.getName());
    }

    @Override
    public void handleMessage(Message message) {
        Exception exception = message.getContent(Exception.class);
        if (exception instanceof SoapFault soapFault) {
			var statusCode = soapFault.getStatusCode();
			var errorMessage = soapFault.getMessage();
			var errorDetail = soapFault.getDetail() != null ? soapFault.getDetail().getTextContent() : "";

            List<QName> subCodes = soapFault.getSubCodes();

            if(subCodes == null) {
                message.setContent(Exception.class, soapFault);
				log.error("Server Gods not happy, sent you a soapFault.. Trying to recover.. {} {} {} subcodes = null", statusCode, errorMessage, errorDetail, soapFault);
				return;
            } else {
				var subcodesparsed = subCodes.stream().filter(Objects::nonNull).map(QName::getLocalPart).collect(joining(", "));
				log.error("Server Gods not happy, sent you a soapFault.. Trying to recover.. {} {} {} subcodes {} ", statusCode, errorMessage, errorDetail, subcodesparsed, soapFault);
			}

            for (QName subCode : subCodes) {
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
		Object token = message.getExchange().getEndpoint().get(TOKEN);
		if (token instanceof UsernameToken usernameToken) {
			log.error("removeToken  token: UsernameToken created={} name={} token={}",  usernameToken.getCreatedTime(), usernameToken.getName(), token);
		} else
		if (token instanceof SecurityToken securityToken) {
			secureLog.error("removeToken  token-type={} token={}", securityToken.getTokenType(), token);
		} else {
			secureLog.error("removeToken  unable to determine token type, token={}", token);
		}
        message.getExchange().getEndpoint().remove(TOKEN);

        message.getExchange().getEndpoint().remove(TOKEN_ID);
        message.getExchange().remove(TOKEN_ID);
        message.getExchange().remove(TOKEN);
        getTokenStore(message).remove(tokenId);
        log.error("Removed token " + tokenId + " from message and tokenStore");
    }

}
