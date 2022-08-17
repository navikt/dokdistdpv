package no.nav.dokdistdpv.config.cxf;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import no.altinn.correspondenceagencyexternalaec.AltinnFault;
import no.altinn.correspondenceagencyexternalaec.CorrespondenceAgencyExternalEC2SF;
import no.altinn.correspondenceagencyexternalaec.ICorrespondenceAgencyExternalEC2;
import no.altinn.correspondenceagencyexternalaec.ICorrespondenceAgencyExternalEC2InsertCorrespondenceECAltinnFaultFaultFaultMessage;
import no.altinn.correspondenceagencyexternalaec.InsertCorrespondenceV2;
import no.altinn.correspondenceagencyexternalaec.ReceiptExternal;
import no.nav.dokdistdpv.config.cxf.mapping.AltinnDokument;
import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse;
import no.nav.dokdistdpv.exception.AltinnException;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.xml.ws.BindingProvider;
import java.util.List;
import java.util.Properties;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;
import static no.nav.dokdistdpv.config.cxf.mapping.AltinnForsendelseMapper.mapToCorrespondence;

@Component
@Slf4j
public class AltinnClient {

	private AltinnProps altinnProps;
	private SecurityCredentials securityCredentials;

	private final ICorrespondenceAgencyExternalEC2 iCorrespondenceAgencyExternalEC2;

	protected AltinnClient(AltinnProps altinnProps,
						   SecurityCredentials securityCredentials) {
		this.altinnProps = altinnProps;
		this.securityCredentials = securityCredentials;
		this.iCorrespondenceAgencyExternalEC2 = getClient();
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

		return port;
	}

	public ReceiptExternal insertCorrespondence(
			String konversasjonId,
			HentForsendelseResponse forsendelse,
			List<AltinnDokument> dokumenter
	) {

		InsertCorrespondenceV2 insertCorrespondenceV2 = mapToCorrespondence(forsendelse, dokumenter, altinnProps.serviceCode, altinnProps.serviceEditionCode);

		log.info("Distribuerer forsendelse med konversasjonId={}, mottaker={} til Altinn", konversasjonId, insertCorrespondenceV2.getReportee());

		try {
			var receipt = iCorrespondenceAgencyExternalEC2.insertCorrespondenceEC(
					altinnProps.username,
					altinnProps.password,
					altinnProps.userCode,
					konversasjonId,
					insertCorrespondenceV2);

			log.info("Kvittering fra Altinn: id={}, text={}, type={}, status={}",
					receipt.getReceiptId(),
					receipt.getReceiptText(),
					receipt.getReceiptTypeName(),
					receipt.getReceiptStatusCode());

			return receipt;

		} catch (ICorrespondenceAgencyExternalEC2InsertCorrespondenceECAltinnFaultFaultFaultMessage e) {
			log.warn(getAltinnFaultAsString(e.getFaultInfo()));
			throw new AltinnException(e.getMessage(), e.getCause());
		}
	}

	private String getAltinnFaultAsString(AltinnFault fault) {

		return "ErrorMessage:" + getSafeString(fault.getAltinnErrorMessage()) + '/' +
				"ExtendedErrorMessage:" + getSafeString(fault.getAltinnExtendedErrorMessage()) + '/' +
				"LocalizedErrorMessage:" + getSafeString(fault.getAltinnLocalizedErrorMessage()) + '/' +
				"ErrorGuid:" + getSafeString(fault.getErrorGuid()) + '/' +
				"ErrorID:" + fault.getErrorID() + '/' +
				"UserGuid:" + getSafeString(fault.getUserGuid()) + '/' +
				"UserId:" + getSafeString(fault.getUserId());
	}

	private String getSafeString(String element) {
		return element != null ? element : "null";
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
