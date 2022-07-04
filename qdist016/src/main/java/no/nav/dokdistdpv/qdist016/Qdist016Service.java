package no.nav.dokdistdpv.qdist016;

import lombok.extern.slf4j.Slf4j;
import no.altinn.correspondenceagencyexternalaec.ICorrespondenceAgencyExternalAEC2InsertCorrespondenceAECV2AltinnFaultFaultFaultMessage;
import no.nav.dokdistdpv.config.cxf.AltinnClient;
import no.nav.dokdistdpv.consumer.rdist001.AdministrerForsendelseConsumer;
import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse;
import no.nav.meldinger.virksomhet.dokdistfordeling.qdist008.out.DistribuerTilKanal;
import org.apache.camel.Handler;
import org.springframework.stereotype.Service;

import static java.util.UUID.randomUUID;
import static no.nav.dokdistdpv.qdist016.Validator.validerForsendelse;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Service
public class Qdist016Service {

	private final AdministrerForsendelseConsumer administrerForsendelseConsumer;
	private final DokumentService dokumentService;
	private final AltinnClient altinnClient;

	public Qdist016Service(AdministrerForsendelseConsumer administrerForsendelseConsumer,
						   DokumentService dokumentService,
						   AltinnClient altinnClient
	) {
		this.administrerForsendelseConsumer = administrerForsendelseConsumer;
		this.dokumentService = dokumentService;
		this.altinnClient = altinnClient;
	}

	@Handler
	public DistribuerTilKanal distribuerForsendelseTilDPV(DistribuerTilKanal distribuerTilKanal) {

		var forsendelseId = distribuerTilKanal.getForsendelseId();
		HentForsendelseResponse forsendelse = hentForsendelse(forsendelseId);
		validerForsendelse(forsendelseId, forsendelse);

		var konversasjonId = genererKonversasjonId(forsendelseId, forsendelse);
		var dokumenter = dokumentService.hentDokumenter(forsendelse);

		//6. Distribuer til Altinn
		try {
			altinnClient.insertCorrespondence(konversasjonId, forsendelse, dokumenter);
		} catch (ICorrespondenceAgencyExternalAEC2InsertCorrespondenceAECV2AltinnFaultFaultFaultMessage e) {
			log.warn("Problem med sending til Altinn");
			// TODO: Gjer handtering av dette betre
		}

		/*
		7. Oppdater forsendelsen
		 */

		return null;
	}

	String genererKonversasjonId(String forsendelseId, HentForsendelseResponse forsendelse) {
		var konversasjonId = forsendelse.konversasjonId();

		if (isBlank(konversasjonId)) {
			konversasjonId = randomUUID().toString();
			administrerForsendelseConsumer.persisterKonversasjonId(forsendelseId, konversasjonId);
		}

		return konversasjonId;
	}

	private HentForsendelseResponse hentForsendelse(String forsendelseId) {
		return administrerForsendelseConsumer.hentForsendelse(forsendelseId);
	}
}
