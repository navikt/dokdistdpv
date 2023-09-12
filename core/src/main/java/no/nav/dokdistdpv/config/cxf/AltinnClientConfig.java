package no.nav.dokdistdpv.config.cxf;

import jakarta.xml.ws.BindingProvider;
import no.altinn.correspondenceagencyexternalaec.CorrespondenceAgencyExternalEC2SF;
import no.altinn.correspondenceagencyexternalaec.ICorrespondenceAgencyExternalEC2;
import no.nav.dokdistdpv.properties.AltinnProperties;
import no.nav.dokdistdpv.properties.DokdistdpvProperties;
import no.nav.dokdistdpv.properties.KeyStoreProperties;
import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static jakarta.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;
import static jakarta.xml.ws.BindingProvider.SESSION_MAINTAIN_PROPERTY;

@Configuration
public class AltinnClientConfig {

	@Bean
	public ICorrespondenceAgencyExternalEC2 iCorrespondenceAgencyExternalEC2(AltinnProperties altinnProperties, Bus bus,
																			 DokdistdpvProperties dokdistdpvProperties,
																			 KeyStoreProperties keyStoreProperties) {
		CorrespondenceAgencyExternalEC2SF service = new CorrespondenceAgencyExternalEC2SF();
		ICorrespondenceAgencyExternalEC2 port = service.getCustomBindingICorrespondenceAgencyExternalEC2();
		BindingProvider bindingProvider = (BindingProvider) port;
		bindingProvider.getRequestContext().put(ENDPOINT_ADDRESS_PROPERTY, altinnProperties.endpoint());

		Client client = getClient(port, keyStoreProperties);
		STSConfigUtil.configureStsRequestSamlToken(client);
		setClientTimeout(client);

		if (dokdistdpvProperties.getQdist016().isAltinnlogg()) {
			client.getInInterceptors().add(new LoggingInInterceptor());
			LoggingOutInterceptor outInterceptor = new LoggingOutInterceptor();
			outInterceptor.setSensitiveElementNames(Set.of("ns2:systemPassword"));
			outInterceptor.setPrettyLogging(true);
			outInterceptor.setLimit(1024 * 1024 * 100);
			client.getOutInterceptors().add(outInterceptor);
			client.getInFaultInterceptors().add(new LoggingInInterceptor());
		}
		return port;
	}

	public Client getClient(ICorrespondenceAgencyExternalEC2 port, KeyStoreProperties keyStoreProperties) {
		Client client = ClientProxy.getClient(port);
		client.getRequestContext().put("security.signature.properties", getKeyStoreProperties(keyStoreProperties));
		client.getRequestContext().put("security.must-understand", true);
		client.getRequestContext().put("org.apache.cxf.message.Message.MAINTAIN_SESSION", true);
		client.getRequestContext().put(SESSION_MAINTAIN_PROPERTY, true);
		client.getRequestContext().put("security.cache.issued.token.in.endpoint", true);
		client.getRequestContext().put("security.issue.after.failed.renew", true);
		client.getRequestContext().put("security.sts.token.imminent-expiry-value", 15);

		return client;
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

	private void setClientTimeout(Client client) {
		HTTPConduit conduit = (HTTPConduit) client.getConduit();
		HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
		httpClientPolicy.setConnectionTimeout(TimeUnit.SECONDS.toMillis(2));
		httpClientPolicy.setReceiveTimeout(TimeUnit.SECONDS.toMillis(20));
		conduit.setClient(httpClientPolicy);
	}
}
