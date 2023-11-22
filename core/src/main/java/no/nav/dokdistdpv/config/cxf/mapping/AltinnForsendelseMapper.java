package no.nav.dokdistdpv.config.cxf.mapping;

import lombok.extern.slf4j.Slf4j;
import no.altinn.correspondenceagencyexternalaec.CorrespondenceStatusFilterV3;
import no.altinn.correspondenceagencyexternalaec.InsertCorrespondenceV2;
import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse;

import java.util.List;

import static java.lang.Integer.parseInt;
import static no.nav.dokdistdpv.config.cxf.mapping.ContentMapper.mapContent;
import static no.nav.dokdistdpv.config.cxf.mapping.NotificationsMapper.mapNotifications;

@Slf4j
public class AltinnForsendelseMapper {

	public static InsertCorrespondenceV2 mapToCorrespondence(
			HentForsendelseResponse forsendelse,
			List<AltinnDokument> dokumenter,
			String serviceCode,
			String serviceEditionCode
	) {
		InsertCorrespondenceV2 insertCorrespondenceV2 = new InsertCorrespondenceV2();

		insertCorrespondenceV2.setServiceCode(serviceCode);
		insertCorrespondenceV2.setServiceEdition(serviceEditionCode);
		insertCorrespondenceV2.setReportee(forsendelse.mottaker().mottakerId());
		insertCorrespondenceV2.setContent(mapContent(forsendelse, dokumenter));
		insertCorrespondenceV2.setNotifications(mapNotifications(forsendelse));

		return insertCorrespondenceV2;
	}

	public static CorrespondenceStatusFilterV3 mapCorrespondenceStatusFilter(String mottakerId,
																			 String konversasjonId,
																			 String serviceCode,
																			 String serviceEditionCode) {
		CorrespondenceStatusFilterV3 correspondenceStatusFilterV3 = new CorrespondenceStatusFilterV3();
		correspondenceStatusFilterV3.setReportee(mottakerId);
		correspondenceStatusFilterV3.setSendersReference(konversasjonId);
		correspondenceStatusFilterV3.setServiceCode(serviceCode);
		correspondenceStatusFilterV3.setServiceEditionCode(parseInt(serviceEditionCode));

		return correspondenceStatusFilterV3;
	}
}
