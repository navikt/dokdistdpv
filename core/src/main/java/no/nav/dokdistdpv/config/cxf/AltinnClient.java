package no.nav.dokdistdpv.config.cxf;

import lombok.extern.slf4j.Slf4j;
import no.altinn.correspondenceagencyexternalaec.ICorrespondenceAgencyExternalEC2;
import no.altinn.correspondenceagencyexternalaec.ICorrespondenceAgencyExternalEC2InsertCorrespondenceECAltinnFaultFaultFaultMessage;
import no.altinn.correspondenceagencyexternalaec.InsertCorrespondenceV2;
import no.altinn.correspondenceagencyexternalaec.ReceiptExternal;
import no.nav.dokdistdpv.config.cxf.mapping.AltinnDokument;
import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse;
import no.nav.dokdistdpv.exception.AltinnException;
import no.nav.dokdistdpv.properties.AltinnProperties;
import org.springframework.stereotype.Component;

import java.util.List;

import static no.nav.dokdistdpv.config.cxf.mapping.AltinnForsendelseMapper.mapToCorrespondence;

@Component
@Slf4j
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
			HentForsendelseResponse forsendelse,
			List<AltinnDokument> dokumenter
	) {

		InsertCorrespondenceV2 insertCorrespondenceV2 = mapToCorrespondence(forsendelse, dokumenter, altinnProperties.serviceCode(),
				altinnProperties.serviceEditionCode());

		try {
			var receipt = iCorrespondenceAgencyExternalEC2.insertCorrespondenceEC(
					altinnProperties.username(),
					altinnProperties.password(),
					altinnProperties.userCode(),
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
}
