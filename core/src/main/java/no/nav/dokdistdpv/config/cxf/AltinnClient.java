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
import no.nav.dokdistdpv.properties.AltinnProperties;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static no.nav.dokdistdpv.config.cxf.mapping.AltinnForsendelseMapper.mapCorrespondenceStatusFilter;

@Slf4j
@Component
public class AltinnClient {

	private final AltinnProperties altinnProperties;
	private final ICorrespondenceAgencyExternalEC2 iCorrespondenceAgencyExternalEC2;

	protected AltinnClient(AltinnProperties altinnProperties,
						   ICorrespondenceAgencyExternalEC2 iCorrespondenceAgencyExternalEC2) {
		this.altinnProperties = altinnProperties;
		this.iCorrespondenceAgencyExternalEC2 = iCorrespondenceAgencyExternalEC2;
	}

	@Retryable(retryFor = SOAPFaultException.class, backoff = @Backoff(delay = 1000))
	public ReceiptExternal insertCorrespondence(
			String konversasjonId,
			InsertCorrespondenceV2 insertCorrespondenceV2
	) {
		try {
			return iCorrespondenceAgencyExternalEC2.insertCorrespondenceEC(
					altinnProperties.username(),
					altinnProperties.password(),
					altinnProperties.userCode(),
					konversasjonId,
					insertCorrespondenceV2);

		} catch (ICorrespondenceAgencyExternalEC2InsertCorrespondenceECAltinnFaultFaultFaultMessage e) {
			log.warn("Error ved distribusjon til Altinn med feilmelding={} og guid={}", getErrorMsg(e.getFaultInfo()), getErrorGuid(e.getFaultInfo()));
			throw new AltinnException(e.getMessage(), e.getCause());
		}
	}

	@Retryable(retryFor = SOAPFaultException.class, backoff = @Backoff(delay = 1000))
	public Optional<CorrespondenceStatusResultV3> hentCorrespondenceStatusResult(String mottakerId, String konversasjonId) {
		log.info("hentCorrespondenceStatusResult har mottatt kall til å hente correspondenceStatus fra altinn for konversasjonId={}", konversasjonId);

		CorrespondenceStatusFilterV3 correspondenceStatusFilterV3 = mapCorrespondenceStatusFilter(mottakerId, konversasjonId,
				altinnProperties.serviceCode(),
				altinnProperties.serviceEditionCode());
		try {
			CorrespondenceStatusResultV3 correspondenceStatusDetailsECV3 = iCorrespondenceAgencyExternalEC2.getCorrespondenceStatusDetailsECV3(altinnProperties.username(), altinnProperties.password(), correspondenceStatusFilterV3);
			return Optional.ofNullable(correspondenceStatusDetailsECV3);
		} catch (ICorrespondenceAgencyExternalEC2GetCorrespondenceStatusDetailsECV3AltinnFaultFaultFaultMessage err) {
			log.warn("Feilet til å hente CorrespondenceStatus fra Altinn med feilmelding={} og guid={}", getErrorMsg(err.getFaultInfo()), getErrorGuid(err.getFaultInfo()));
			return Optional.empty();
		}
	}

	private static String getErrorGuid(AltinnFault fault) {
		return fault.getUserGuid() != null ? fault.getErrorGuid() : "Ukjent GUID";
	}

	private static String getErrorMsg(AltinnFault fault) {
		return fault.getAltinnErrorMessage() != null ? fault.getAltinnErrorMessage() : "Ukjent feil";
	}

}