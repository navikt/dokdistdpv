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

import static java.lang.String.format;
import static org.apache.cxf.phase.Phase.UNMARSHAL;
import static org.apache.cxf.ws.security.SecurityConstants.TOKEN;
import static org.apache.cxf.ws.security.SecurityConstants.TOKEN_ID;
import static org.apache.cxf.ws.security.tokenstore.TokenStoreUtils.getTokenStore;

/**
 * Interceptor for å håndtere feil med token mot Altinn.
 */
@Slf4j
public class InvalidTokenInterceptor extends AbstractPhaseInterceptor<Message> {

	private static final String ERROR_CODE_BAD_CONTEXT_TOKEN = "BadContextToken";
	private static final String ERROR_CODE_INVALID_SECURITY = "InvalidSecurity";

	public InvalidTokenInterceptor() {
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

			if (subCodes == null) {
				message.setContent(Exception.class, soapFault);
				log.error("SOAP-kall feilet med subCodes=null. status={}, errorMessage={} og errorDetail={}", statusCode, errorMessage, errorDetail, soapFault);
				return;
			}

			var shouldRemoveInvalidToken = subCodes.stream()
					.anyMatch(InvalidTokenInterceptor::isBadContextTokenOrInvalidSecurity);

			if (shouldRemoveInvalidToken) {
				String tokenId = (String) message.getContextualProperty(TOKEN_ID);

				try {
					removeTokenFromMessageAndTokenStore(message, tokenId);
				} catch (TokenStoreException e) {
					log.error("Klarte ikke åpne TokenStore", e);
				}

				CookieStore.setCookie(null);
				soapFault.setMessage(format("Token=%s er fjernet fra TokenStore. Nytt token vil bli utstedt ved neste kall. soapFault.message=%s", tokenId, errorMessage));
				message.setContent(Exception.class, soapFault);
			}
		}
	}

	private static boolean isBadContextTokenOrInvalidSecurity(QName subCode) {
		return subCode.getLocalPart().equalsIgnoreCase(ERROR_CODE_BAD_CONTEXT_TOKEN)
			   || subCode.getLocalPart().equalsIgnoreCase(ERROR_CODE_INVALID_SECURITY);
	}

	private void removeTokenFromMessageAndTokenStore(Message message, String tokenId) throws TokenStoreException {
		message.getExchange().getEndpoint().remove(TOKEN);
		message.getExchange().getEndpoint().remove(TOKEN_ID);
		message.getExchange().remove(TOKEN_ID);
		message.getExchange().remove(TOKEN);
		getTokenStore(message).remove(tokenId);

		log.info("Fjernet tokenId={} fra message og TokenStore", tokenId);
	}
}
