package no.nav.dokdistdpv.qdist016;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpv.config.cxf.AltinnClient;
import no.nav.dokdistdpv.consumer.rdist001.AdministrerForsendelseConsumer;
import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse;
import no.nav.meldinger.virksomhet.dokdistfordeling.qdist008.out.DistribuerTilKanal;
import org.apache.camel.Handler;
import org.springframework.stereotype.Service;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static no.nav.dokdistdpv.qdist016.Validator.validerForsendelse;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Service
public class Qdist016Service {

	private static final String FORSENDELSE_STATUS_OVERSENDT = "OVERSENDT";

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
	public String distribuerForsendelseTilDPV(DistribuerTilKanal distribuerTilKanal) {

		var forsendelseId = distribuerTilKanal.getForsendelseId();
		HentForsendelseResponse forsendelse = administrerForsendelseConsumer.hentForsendelse(forsendelseId);
		validerForsendelse(forsendelseId, forsendelse);

		var konversasjonId = genererKonversasjonId(forsendelseId, forsendelse);
		var dokumenter = dokumentService.hentDokumenter(forsendelse);

		altinnClient.insertCorrespondence(konversasjonId, forsendelse, dokumenter);
		administrerForsendelseConsumer.oppdaterStatus(forsendelseId, FORSENDELSE_STATUS_OVERSENDT);

		log.info(format("qdist016 har distribuert forsendelse med id=%s til Altinn", forsendelseId));

		return forsendelseId;
	}

	String genererKonversasjonId(String forsendelseId, HentForsendelseResponse forsendelse) {
		var konversasjonId = forsendelse.konversasjonId();

		if (isBlank(konversasjonId)) {
			konversasjonId = randomUUID().toString();
			administrerForsendelseConsumer.persisterKonversasjonId(forsendelseId, konversasjonId);
		}

		return konversasjonId;
	}
}
