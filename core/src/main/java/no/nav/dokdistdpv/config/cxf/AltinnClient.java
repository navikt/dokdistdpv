package no.nav.dokdistdpv.config.cxf;

import lombok.extern.slf4j.Slf4j;
import no.altinn.correspondenceagencyexternalaec.CorrespondenceStatusFilterV3;
import no.altinn.correspondenceagencyexternalaec.CorrespondenceStatusResultV3;
import no.altinn.correspondenceagencyexternalaec.ICorrespondenceAgencyExternalEC2;
import no.altinn.correspondenceagencyexternalaec.ICorrespondenceAgencyExternalEC2GetCorrespondenceStatusDetailsECV3AltinnFaultFaultFaultMessage;
import no.altinn.correspondenceagencyexternalaec.ICorrespondenceAgencyExternalEC2InsertCorrespondenceECAltinnFaultFaultFaultMessage;
import no.altinn.correspondenceagencyexternalaec.InsertCorrespondenceV2;
import no.altinn.correspondenceagencyexternalaec.ReceiptExternal;
import no.nav.dokdistdpv.exception.AltinnException;
import no.nav.dokdistdpv.properties.AltinnProperties;
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

	public ReceiptExternal insertCorrespondence(
			String konversasjonId,
			InsertCorrespondenceV2 insertCorrespondenceV2
	) {

		try {
			var receipt = iCorrespondenceAgencyExternalEC2.insertCorrespondenceEC(
					altinnProperties.username(),
					altinnProperties.password(),
					altinnProperties.userCode(),
					konversasjonId,
					insertCorrespondenceV2);


			return receipt;

		} catch (ICorrespondenceAgencyExternalEC2InsertCorrespondenceECAltinnFaultFaultFaultMessage e) {
			logError(e);
			throw new AltinnException(e.getMessage(), e.getCause());
		}
	}

	public Optional<CorrespondenceStatusResultV3> hentCorrespondenceStatusResult(String mottakerId, String konversasjonId) {

		CorrespondenceStatusFilterV3 correspondenceStatusFilterV3 = mapCorrespondenceStatusFilter(mottakerId, konversasjonId,
				altinnProperties.serviceCode(),
				altinnProperties.serviceEditionCode());
		try {
			CorrespondenceStatusResultV3 correspondenceStatusDetailsECV3 = iCorrespondenceAgencyExternalEC2.getCorrespondenceStatusDetailsECV3(altinnProperties.username(), altinnProperties.password(), correspondenceStatusFilterV3);
			return Optional.ofNullable(correspondenceStatusDetailsECV3);
		} catch (ICorrespondenceAgencyExternalEC2GetCorrespondenceStatusDetailsECV3AltinnFaultFaultFaultMessage err) {
			logError(err);
			return Optional.empty();
		}
	}

	private static void logError(Exception e) {
		if (e instanceof ICorrespondenceAgencyExternalEC2InsertCorrespondenceECAltinnFaultFaultFaultMessage err) {
			var errorMsg = err.getFaultInfo().getAltinnErrorMessage() != null ? err.getFaultInfo().getAltinnErrorMessage() : "Ukjent feil";
			var errorGuid = err.getFaultInfo().getUserGuid() != null ? err.getFaultInfo().getErrorGuid() : "Ukjent GUID";
			log.warn("Error ved distribusjon til Altinn med feilmelding={} og guid={}", errorMsg, errorGuid);
		}
		if (e instanceof ICorrespondenceAgencyExternalEC2GetCorrespondenceStatusDetailsECV3AltinnFaultFaultFaultMessage err) {
			var errorMsg = err.getFaultInfo().getAltinnErrorMessage() != null ? err.getFaultInfo().getAltinnErrorMessage() : "Ukjent feil";
			var errorGuid = err.getFaultInfo().getUserGuid() != null ? err.getFaultInfo().getErrorGuid() : "Ukjent GUID";
			log.warn("Feilet til å hente CorrespondenceStatus fra Altinn med feilmelding={} og guid={}", errorMsg, errorGuid);
		}
	}
}
