package no.nav.dokdistdpv.qdist016.itest;

import no.altinn.correspondenceagencyexternalaec.ICorrespondenceAgencyExternalEC2;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HttpClientHTTPConduit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


@Profile("itest")
@Configuration
public class AltinnTestConfig {

	private final ICorrespondenceAgencyExternalEC2 iCorrespondenceAgencyExternalEC2;

	public AltinnTestConfig(ICorrespondenceAgencyExternalEC2 iCorrespondenceAgencyExternalEC2) {
		this.iCorrespondenceAgencyExternalEC2 = iCorrespondenceAgencyExternalEC2;
	}

	@Bean
	public Client getClient() {
		Client client = ClientProxy.getClient(iCorrespondenceAgencyExternalEC2);
		client.getRequestContext().put(HttpClientHTTPConduit.FORCE_HTTP_VERSION, "1.1");
		return client;
	}

}
