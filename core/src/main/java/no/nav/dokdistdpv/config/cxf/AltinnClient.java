package no.nav.dokdistdpv.config.cxf;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import no.altinn.correspondenceagencyexternalaec.CorrespondenceAgencyExternalEC2SF;
import no.altinn.correspondenceagencyexternalaec.ICorrespondenceAgencyExternalEC2;
import no.altinn.correspondenceagencyexternalaec.ICorrespondenceAgencyExternalEC2InsertCorrespondenceECAltinnFaultFaultFaultMessage;
import no.altinn.correspondenceagencyexternalaec.InsertCorrespondenceV2;
import no.altinn.correspondenceagencyexternalaec.ReceiptExternal;
import no.nav.dokdistdpv.config.cxf.mapping.AltinnDokument;
import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse;
import no.nav.dokdistdpv.exception.AltinnException;
import no.nav.dokdistdpv.properties.DokdistdpvProperties;
import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.frontend.ClientProxy;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.xml.ws.BindingProvider;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;
import static no.nav.dokdistdpv.config.cxf.mapping.AltinnForsendelseMapper.mapToCorrespondence;

@Component
@Slf4j
public class AltinnClient {

	private final DokdistdpvProperties dokdistdpvProperties;
	// Autowiring av altinnProps, securityCredentials, bus sikrer at konfigurerte verdier blir injected etter at ressursene er konfigurert
	private AltinnProps altinnProps;
	private SecurityCredentials securityCredentials;
	private Bus bus;

	private final ICorrespondenceAgencyExternalEC2 iCorrespondenceAgencyExternalEC2;

	protected AltinnClient(DokdistdpvProperties dokdistdpvProperties,
						   AltinnProps altinnProps,
						   SecurityCredentials securityCredentials,
						   Bus bus) {
		this.dokdistdpvProperties = dokdistdpvProperties;
		this.altinnProps = altinnProps;
		this.securityCredentials = securityCredentials;
		this.iCorrespondenceAgencyExternalEC2 = getClient();
		this.bus = bus;
	}

	private ICorrespondenceAgencyExternalEC2 getClient() {
		CorrespondenceAgencyExternalEC2SF service = new CorrespondenceAgencyExternalEC2SF();
		ICorrespondenceAgencyExternalEC2 port = service.getCustomBindingICorrespondenceAgencyExternalEC2();
		BindingProvider bindingProvider = (BindingProvider) port;
		bindingProvider.getRequestContext().put(ENDPOINT_ADDRESS_PROPERTY, altinnProps.endpoint);

		Client client = ClientProxy.getClient(port);
		client.getRequestContext().put("security.signature.properties", SecurityCredentials.keyStoreProperties);
		client.getRequestContext().put("security.must-understand", true);
		client.getRequestContext().put("org.apache.cxf.message.Message.MAINTAIN_SESSION", true);
		client.getRequestContext().put("javax.xml.ws.session.maintain", true);
		client.getRequestContext().put("security.cache.issued.token.in.endpoint", true);
		client.getRequestContext().put("security.issue.after.failed.renew", true);
		if(dokdistdpvProperties.getQdist016().isAltinnlogg()) {
			client.getInInterceptors().add(new LoggingInInterceptor());
			LoggingOutInterceptor outInterceptor = new LoggingOutInterceptor();
			outInterceptor.setSensitiveElementNames(Set.of("*:systemPassword"));
			outInterceptor.setPrettyLogging(true);
			outInterceptor.setLimit(1024 * 1024 * 100);
			client.getOutInterceptors().add(outInterceptor);
			client.getInFaultInterceptors().add(new LoggingInInterceptor());
		}
		return port;
	}

	public ReceiptExternal insertCorrespondence(
			String konversasjonId,
			HentForsendelseResponse forsendelse,
			List<AltinnDokument> dokumenter
	) {

		InsertCorrespondenceV2 insertCorrespondenceV2 = mapToCorrespondence(forsendelse, dokumenter, altinnProps.serviceCode, altinnProps.serviceEditionCode);

		try {
			var receipt = iCorrespondenceAgencyExternalEC2.insertCorrespondenceEC(
					altinnProps.username,
					altinnProps.password,
					altinnProps.userCode,
					konversasjonId,
					insertCorrespondenceV2);


			return receipt;

		} catch (ICorrespondenceAgencyExternalEC2InsertCorrespondenceECAltinnFaultFaultFaultMessage e) {
			var errorMsg = e.getFaultInfo().getAltinnErrorMessage() != null ? e.getFaultInfo().getAltinnErrorMessage() : "Ukjent feil";
			var errorGuid = e.getFaultInfo().getUserGuid() != null ? e.getFaultInfo().getErrorGuid() : "Ukjent GUID";
			log.warn("Error ved distribusjon til Altinn med feilmelding={} og guid={}", errorMsg, errorGuid);
			throw new AltinnException(e.getMessage(), e.getCause());
		}
	}


	@ConfigurationProperties("altinn")
	public record AltinnProps(
			@NotNull String endpoint,
			@NotNull String username,
			@NotNull @ToString.Exclude String password,
			@NotNull String userCode,
			@NotBlank String serviceCode,
			@NotBlank String serviceEditionCode) {
	}

	@ConfigurationProperties(prefix = "virksomhetssertifikat")
	public record SecurityCredentials(
			@NotBlank String path,
			@NotBlank String password,
			@NotBlank String alias) {

		public static final Properties keyStoreProperties = new Properties();

		public SecurityCredentials {
			keyStoreProperties.setProperty("org.apache.ws.security.crypto.provider", "org.apache.ws.security.components.crypto.Merlin");
			keyStoreProperties.setProperty("org.apache.ws.security.crypto.merlin.keystore.file", path);
			keyStoreProperties.setProperty("org.apache.ws.security.crypto.merlin.keystore.password", password);
			keyStoreProperties.setProperty("org.apache.ws.security.crypto.merlin.keystore.type", "pkcs12");
			keyStoreProperties.setProperty("org.apache.ws.security.crypto.merlin.keystore.private.password", password);
			keyStoreProperties.setProperty("org.apache.ws.security.crypto.merlin.keystore.alias", alias);
		}

	}

}
