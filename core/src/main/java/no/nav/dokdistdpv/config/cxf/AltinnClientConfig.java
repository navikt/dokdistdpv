package no.nav.dokdistdpv.config.cxf;

import jakarta.xml.ws.BindingProvider;
import no.altinn.correspondenceagencyexternalaec.CorrespondenceAgencyExternalEC2SF;
import no.altinn.correspondenceagencyexternalaec.ICorrespondenceAgencyExternalEC2;
import no.nav.dokdistdpv.properties.AltinnProperties;
import no.nav.dokdistdpv.properties.KeyStoreProperties;
import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

import static jakarta.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;
import static jakarta.xml.ws.BindingProvider.SESSION_MAINTAIN_PROPERTY;
import static java.lang.String.valueOf;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.apache.cxf.message.Message.RECEIVE_TIMEOUT;
import static org.apache.cxf.rt.security.SecurityConstants.CACHE_ISSUED_TOKEN_IN_ENDPOINT;
import static org.apache.cxf.rt.security.SecurityConstants.STS_ISSUE_AFTER_FAILED_RENEW;
import static org.apache.cxf.rt.security.SecurityConstants.STS_TOKEN_IMMINENT_EXPIRY_VALUE;

@Configuration
public class AltinnClientConfig {

	@Bean
	public ICorrespondenceAgencyExternalEC2 iCorrespondenceAgencyExternalEC2(AltinnProperties altinnProperties,
																			 Bus bus,
																			 KeyStoreProperties keyStoreProperties) {
		CorrespondenceAgencyExternalEC2SF service = new CorrespondenceAgencyExternalEC2SF();
		ICorrespondenceAgencyExternalEC2 port = service.getCustomBindingICorrespondenceAgencyExternalEC2();
		BindingProvider bindingProvider = (BindingProvider) port;
		bindingProvider.getRequestContext().put(ENDPOINT_ADDRESS_PROPERTY, altinnProperties.endpoint());

		configureClient(port, keyStoreProperties);
		return port;
	}

	public void configureClient(ICorrespondenceAgencyExternalEC2 port, KeyStoreProperties keyStoreProperties) {
		Client client = ClientProxy.getClient(port);
		client.getRequestContext().put("security.signature.properties", getKeyStoreProperties(keyStoreProperties));
		client.getRequestContext().put("security.must-understand", true);
		client.getRequestContext().put("org.apache.cxf.message.Message.MAINTAIN_SESSION", true);
		client.getRequestContext().put(SESSION_MAINTAIN_PROPERTY, true);
		client.getRequestContext().put(CACHE_ISSUED_TOKEN_IN_ENDPOINT, true);
		client.getRequestContext().put(STS_ISSUE_AFTER_FAILED_RENEW, true);
		client.getRequestContext().put(STS_TOKEN_IMMINENT_EXPIRY_VALUE, 15);
		client.getRequestContext().put(RECEIVE_TIMEOUT, valueOf(MINUTES.toMillis(3)));
	}

	private Properties getKeyStoreProperties(KeyStoreProperties keyStoreProperties) {
		final Properties properties = new Properties();
		properties.setProperty("org.apache.ws.security.crypto.provider", "org.apache.ws.security.components.crypto.Merlin");
		properties.setProperty("org.apache.ws.security.crypto.merlin.keystore.file", keyStoreProperties.path());
		properties.setProperty("org.apache.ws.security.crypto.merlin.keystore.password", keyStoreProperties.password());
		properties.setProperty("org.apache.ws.security.crypto.merlin.keystore.type", "pkcs12");
		properties.setProperty("org.apache.ws.security.crypto.merlin.keystore.private.password", keyStoreProperties.password());
		properties.setProperty("org.apache.ws.security.crypto.merlin.keystore.alias", keyStoreProperties.alias());
		return properties;
	}

}
