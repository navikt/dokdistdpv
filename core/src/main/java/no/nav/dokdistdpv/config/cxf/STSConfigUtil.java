package no.nav.dokdistdpv.config.cxf;

import no.altinn.correspondenceagencyexternalaec.CorrespondenceAgencyExternalEC2SF;
import org.apache.cxf.binding.soap.Soap12;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.ws.policy.EndpointPolicy;
import org.apache.cxf.ws.policy.PolicyBuilder;
import org.apache.cxf.ws.policy.PolicyEngine;
import org.apache.cxf.ws.policy.attachment.reference.ReferenceResolver;
import org.apache.cxf.ws.policy.attachment.reference.RemoteReferenceResolver;
import org.apache.cxf.ws.security.trust.STSClient;
import org.apache.neethi.Policy;

import static org.apache.cxf.rt.security.SecurityConstants.CACHE_ISSUED_TOKEN_IN_ENDPOINT;
import static org.apache.cxf.rt.security.SecurityConstants.STS_CLIENT;

public class STSConfigUtil {

	private static final String STS_REQUEST_SAML_POLICY = "classpath:sts/policies/requestSamlPolicy.xml";
	private static final String STS_CLIENT_AUTHENTICATION_POLICY = "classpath:sts/policies/untPolicy.xml";

	private STSConfigUtil(){}

	public static void configureStsRequestSamlToken(Client client) {
		STSClient stsClient = new STSClient(client.getBus());
		configureSTSClient(stsClient);

		client.getRequestContext().put(STS_CLIENT, stsClient);
		//Using CXF cache
		client.getRequestContext().put(CACHE_ISSUED_TOKEN_IN_ENDPOINT, true);

		setClientEndpointPolicy(client, resolvePolicyReference(client));
	}

	protected static void configureSTSClient(STSClient stsClient) {
		stsClient.setEnableAppliesTo(false);
		stsClient.setAllowRenewing(false);
		stsClient.setWsdlLocation(CorrespondenceAgencyExternalEC2SF.WSDL_LOCATION.toString());

		//used for the STS client to authenticate itself to the STS provider.
		stsClient.setPolicy(STS_CLIENT_AUTHENTICATION_POLICY);
	}

	private static Policy resolvePolicyReference(Client client) {
		PolicyBuilder policyBuilder = client.getBus().getExtension(PolicyBuilder.class);
		ReferenceResolver resolver = new RemoteReferenceResolver("", policyBuilder);
		return resolver.resolveReference(STS_REQUEST_SAML_POLICY);
	}

	private static void setClientEndpointPolicy(Client client, Policy policy) {
		Endpoint endpoint = client.getEndpoint();
		EndpointInfo endpointInfo = endpoint.getEndpointInfo();

		PolicyEngine policyEngine = client.getBus().getExtension(PolicyEngine.class);
		SoapMessage message = new SoapMessage(Soap12.getInstance());
		EndpointPolicy endpointPolicy = policyEngine.getClientEndpointPolicy(endpointInfo, null, message);
		policyEngine.setClientEndpointPolicy(endpointInfo, endpointPolicy.updatePolicy(policy, message));
	}
}
