package no.nav.dokdistdpv.config.cxf;

import jakarta.xml.ws.soap.SOAPFaultException;
import lombok.extern.slf4j.Slf4j;
import no.altinn.correspondenceagencyexternalaec.AltinnFault;
import no.altinn.correspondenceagencyexternalaec.CorrespondenceStatusFilterV3;
import no.altinn.correspondenceagencyexternalaec.CorrespondenceStatusResultV3;
import no.altinn.correspondenceagencyexternalaec.ICorrespondenceAgencyExternalEC2;
import no.altinn.correspondenceagencyexternalaec.ICorrespondenceAgencyExternalEC2GetCorrespondenceStatusDetailsECV3AltinnFaultFaultFaultMessage;
import no.altinn.correspondenceagencyexternalaec.ICorrespondenceAgencyExternalEC2InsertCorrespondenceECAltinnFaultFaultFaultMessage;
import no.altinn.correspondenceagencyexternalaec.InsertCorrespondenceV2;
import no.altinn.correspondenceagencyexternalaec.ReceiptExternal;
import no.nav.dokdistdpv.exception.AltinnException;
import no.nav.dokdistdpv.exception.DokdistdpvTechnicalException;
import no.nav.dokdistdpv.properties.AltinnProperties;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static no.nav.dokdistdpv.config.cxf.mapping.AltinnForsendelseMapper.mapCorrespondenceStatusFilter;

@Slf4j
@Component
public class AltinnClient {

	private static final int FALLBACK_ERROR_ID = -999;
	private static final String FALLBACK_ALTINN_ERROR_MESSAGE = "AltinnErrorMessage var ikke satt på responsen";

	private final AltinnProperties altinnProperties;
	private final ICorrespondenceAgencyExternalEC2 iCorrespondenceAgencyExternalEC2;

	protected AltinnClient(AltinnProperties altinnProperties,
						   ICorrespondenceAgencyExternalEC2 iCorrespondenceAgencyExternalEC2) {
		this.altinnProperties = altinnProperties;
		this.iCorrespondenceAgencyExternalEC2 = iCorrespondenceAgencyExternalEC2;
	}

	@Retryable(retryFor = {SOAPFaultException.class, DokdistdpvTechnicalException.class})
	public ReceiptExternal insertCorrespondence(String konversasjonId, InsertCorrespondenceV2 insertCorrespondenceV2) {
		log.info("Skal distribuere forsendelse med konversasjonId={} til Altinn", konversasjonId);

		try {
			return iCorrespondenceAgencyExternalEC2.insertCorrespondenceEC(
					altinnProperties.username(),
					altinnProperties.password(),
					altinnProperties.userCode(),
					konversasjonId,
					insertCorrespondenceV2);
		} catch (ICorrespondenceAgencyExternalEC2InsertCorrespondenceECAltinnFaultFaultFaultMessage e) {
			AltinnFault faultInfo = e.getFaultInfo();
			// https://altinn.github.io/docs/api/tjenesteeiere/soap/feilhandtering/
			int errorId = getErrorId(faultInfo);
			String errorMsg = getErrorMsg(faultInfo);
			String errorGuid = getErrorGuid(faultInfo);
			if (errorMsg.contains("non-functional error")) {
				log.error("Distribusjon til Altinn feilet teknisk med errorId={}, feilmelding={}, guid={}", errorId, errorMsg, errorGuid);
				throw new DokdistdpvTechnicalException(e.getMessage(), e);
			} else {
				log.warn("Distribusjon til Altinn feilet funksjonelt med errorId={}, feilmelding={}, guid={}", errorId, errorMsg, errorGuid);
				throw new AltinnException(e.getMessage(), e.getCause());
			}
		} catch (SOAPFaultException e) {
			log.warn("Distribusjon til Altinn feilet med feilmelding={} ", e.getMessage(), e);
			throw e;
		}
	}

	@Retryable(retryFor = SOAPFaultException.class)
	public Optional<CorrespondenceStatusResultV3> hentCorrespondenceStatusResult(String mottakerId, String konversasjonId) {
		log.info("Skal hente status for forsendelse med konversasjonId={} fra Altinn", konversasjonId);

		CorrespondenceStatusFilterV3 correspondenceStatusFilterV3 = mapCorrespondenceStatusFilter(
				mottakerId,
				konversasjonId,
				altinnProperties.serviceCode(),
				altinnProperties.serviceEditionCode()
		);

		try {
			CorrespondenceStatusResultV3 correspondenceStatusDetailsECV3 = iCorrespondenceAgencyExternalEC2.getCorrespondenceStatusDetailsECV3(
					altinnProperties.username(),
					altinnProperties.password(),
					correspondenceStatusFilterV3);

			return Optional.ofNullable(correspondenceStatusDetailsECV3);
		} catch (ICorrespondenceAgencyExternalEC2GetCorrespondenceStatusDetailsECV3AltinnFaultFaultFaultMessage err) {
			log.warn("Henting av status for forsendelse med konversasjonId={} fra Altinn feilet med feilmelding={} og guid={}", konversasjonId, getErrorMsg(err.getFaultInfo()), getErrorGuid(err.getFaultInfo()));
			return Optional.empty();
		} catch (SOAPFaultException e) {
			log.warn("Henting av status for forsendelse med konversasjonId={} fra Altinn feilet med feilmelding={}", konversasjonId, e.getMessage(), e);
			throw e;
		}
	}

	private static String getErrorGuid(AltinnFault fault) {
		return fault.getUserGuid() != null ? fault.getErrorGuid() : "Ukjent GUID";
	}

	private static String getErrorMsg(AltinnFault fault) {
		return fault.getAltinnErrorMessage() != null ?
				"error[%s], extendedError[%s], localizedError[%s]"
						.formatted(fault.getAltinnErrorMessage(), fault.getAltinnExtendedErrorMessage(), fault.getAltinnLocalizedErrorMessage()) :
				FALLBACK_ALTINN_ERROR_MESSAGE;
	}

	private static int getErrorId(AltinnFault fault) {
		return fault.getErrorID() != null ? fault.getErrorID() : FALLBACK_ERROR_ID;
	}

}