package no.nav.dokdistdpv.config.cxf.mapping;

import lombok.extern.slf4j.Slf4j;
import no.altinn.correspondenceagencyexternalaec.InsertCorrespondenceV2;
import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse;

import java.util.List;

import static no.nav.dokdistdpv.config.cxf.mapping.ContentMapper.mapContent;
import static no.nav.dokdistdpv.config.cxf.mapping.NotificationsMapper.mapNotifications;

@Slf4j
public class AltinnForsendelseMapper {

	public static final String LANGUAGE_CODE_BOKMAAL = "1044";
	public static final String FROM_ADDRESS = "ikke-besvar-denne@nav.no";

	public static InsertCorrespondenceV2 mapToCorrespondence(
			HentForsendelseResponse forsendelse,
			List<AltinnDokument> dokumenter,
			String serviceCode,
			String serviceEditionCode
	) {

		log.info("Mapper til altinn forsendelse. serviceCode={}, serviceEditionCode={}, reportee={}, ", serviceCode, serviceEditionCode, forsendelse.mottaker().mottakerId());

		InsertCorrespondenceV2 insertCorrespondenceV2 = new InsertCorrespondenceV2();

		insertCorrespondenceV2.setServiceCode(serviceCode);
		insertCorrespondenceV2.setServiceEdition(serviceEditionCode);
		insertCorrespondenceV2.setReportee(forsendelse.mottaker().mottakerId());
		insertCorrespondenceV2.setContent(mapContent(forsendelse, dokumenter));
		insertCorrespondenceV2.setNotifications(mapNotifications(forsendelse.distribusjonstype()));

		return insertCorrespondenceV2;
	}
}
