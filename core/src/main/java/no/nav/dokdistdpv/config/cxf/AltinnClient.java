package no.nav.dokdistdpv.config.cxf;

import no.altinn.correspondenceagencyexternalaec.CorrespondenceAgencyExternalAEC2SF;
import no.altinn.correspondenceagencyexternalaec.ICorrespondenceAgencyExternalAEC2;
import no.altinn.correspondenceagencyexternalaec.ICorrespondenceAgencyExternalAEC2InsertCorrespondenceAECV2AltinnFaultFaultFaultMessage;
import no.altinn.correspondenceagencyexternalaec.InsertCorrespondenceV2;
import no.altinn.correspondenceagencyexternalaec.ReceiptExternal;
import no.nav.dokdistdpv.config.cxf.mapping.AltinnDokument;
import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.springframework.stereotype.Component;

import javax.xml.ws.BindingProvider;
import java.util.List;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;
import static no.nav.dokdistdpv.config.cxf.AltinnClientConfig.ALTINN_INSERT_CORRESPONDENCE_SERVICE_ENDPOINT;
import static no.nav.dokdistdpv.config.cxf.mapping.AltinnForsendelseMapper.mapToCorrespondence;

@Component
public class AltinnClient {

	private final ServiceCode serviceCode;

	private final ICorrespondenceAgencyExternalAEC2 iCorrespondenceAgencyExternalAEC2;

	protected AltinnClient(ServiceCode serviceCode) {
		this.serviceCode = serviceCode;
		this.iCorrespondenceAgencyExternalAEC2 = getClient();
	}

	private ICorrespondenceAgencyExternalAEC2 getClient() {
		CorrespondenceAgencyExternalAEC2SF service = new CorrespondenceAgencyExternalAEC2SF();
		ICorrespondenceAgencyExternalAEC2 port = service.getCustomBindingICorrespondenceAgencyExternalAEC2();
		BindingProvider bindingProvider = (BindingProvider) port;
		bindingProvider.getRequestContext().put(ENDPOINT_ADDRESS_PROPERTY, ALTINN_INSERT_CORRESPONDENCE_SERVICE_ENDPOINT);

		Client client = ClientProxy.getClient(port);
		client.getRequestContext().put("security.signature.properties", SecurityCredentials.keyStoreProperties);
		client.getRequestContext().put("security.must-understand", true);
		client.getRequestContext().put("org.apache.cxf.message.Message.MAINTAIN_SESSION", true);
		client.getRequestContext().put("javax.xml.ws.session.maintain", true);
		client.getRequestContext().put("security.cache.issued.token.in.endpoint", true);
		client.getRequestContext().put("security.issue.after.failed.renew", true);

		return port;
	}

	public ReceiptExternal insertCorrespondence(
			String konversasjonId,
			HentForsendelseResponse forsendelse,
			List<AltinnDokument> dokumenter
	) throws ICorrespondenceAgencyExternalAEC2InsertCorrespondenceAECV2AltinnFaultFaultFaultMessage {
		InsertCorrespondenceV2 insertCorrespondenceV2 = mapToCorrespondence(forsendelse, dokumenter, serviceCode);

		return iCorrespondenceAgencyExternalAEC2.insertCorrespondenceAECV2(konversasjonId, insertCorrespondenceV2);
	}
}
