package no.nav.dokdistdpv.consumer.token;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.Map;

public class NaisTexasRequestInterceptor implements ClientHttpRequestInterceptor {

	public static final String ENTRA_TARGET_SCOPE = "entraTargetScope";
	public static final String MASKINPORTEN_TARGET_SCOPES = "maskinportenTargetScopes";
	private final NaisTexasConsumer naisTexasConsumer;

	public NaisTexasRequestInterceptor(NaisTexasConsumer naisTexasConsumer) {
		this.naisTexasConsumer = naisTexasConsumer;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
		Map<String, Object> attributes = request.getAttributes();

		if (attributes.containsKey(ENTRA_TARGET_SCOPE)) {
			String entraTargetScope = (String) attributes.get(ENTRA_TARGET_SCOPE);
			request.getHeaders().setBearerAuth(naisTexasConsumer.getSystemToken(entraTargetScope));
		} else if (attributes.containsKey(MASKINPORTEN_TARGET_SCOPES)) {
			String maskinportenTargetScopes = (String) attributes.get(MASKINPORTEN_TARGET_SCOPES);
			request.getHeaders().setBearerAuth(naisTexasConsumer.getMaskinportenToken(maskinportenTargetScopes));
		}

		return execution.execute(request, body);
	}
}