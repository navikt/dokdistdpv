package no.nav.dokdistdpv.qdist016;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpv.consumer.rdist001.AdministrerForsendelseConsumer;
import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse;
import no.nav.dokdistdpv.properties.DokdistdpvProperties;
import no.nav.dokdistdpv.qdist016.altinn2.Altinn2MeldingService;
import no.nav.dokdistdpv.qdist016.altinn3.Altinn3MeldingService;
import no.nav.meldinger.virksomhet.dokdistfordeling.qdist008.out.DistribuerTilKanal;
import org.apache.camel.Handler;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static no.nav.dokdistdpv.consumer.rdist001.domain.OppdaterForsendelseRequest.ekspedert;
import static no.nav.dokdistdpv.consumer.rdist001.domain.OppdaterForsendelseRequest.oversendt;
import static no.nav.dokdistdpv.qdist016.Validator.validerForsendelse;

@Slf4j
@Service
public class Qdist016Service {

	private final AdministrerForsendelseConsumer administrerForsendelseConsumer;
	private final Altinn2MeldingService altinn2MeldingService;
	private final Altinn3MeldingService altinn3MeldingService;
	private final boolean distribuerTilAltinn3;

	public Qdist016Service(AdministrerForsendelseConsumer administrerForsendelseConsumer,
						   Altinn2MeldingService altinn2MeldingService,
						   Altinn3MeldingService altinn3MeldingService,
						   DokdistdpvProperties dokdistdpvProperties
	) {
		this.administrerForsendelseConsumer = administrerForsendelseConsumer;
		this.altinn2MeldingService = altinn2MeldingService;
		this.altinn3MeldingService = altinn3MeldingService;
		this.distribuerTilAltinn3 = dokdistdpvProperties.getQdist016().isAltinn3();
	}

	@SuppressWarnings("unused")
	@Handler
	public String distribuerForsendelseTilDPV(DistribuerTilKanal distribuerTilKanal) {
		String forsendelseId = distribuerTilKanal.getForsendelseId();
		HentForsendelseResponse forsendelse = administrerForsendelseConsumer.hentForsendelse(forsendelseId);
		validerForsendelse(forsendelseId, forsendelse);

		if(distribuerTilAltinn3) {
			distribuerTilAltinn3(forsendelse);
		} else {
			distribuerTilAltinn2(forsendelse);
		}

		return forsendelseId;
	}

	private void distribuerTilAltinn3(HentForsendelseResponse forsendelse) {
		UUID konversasjonId = altinn3MeldingService.distribuer(forsendelse);
		log.info("qdist016 Forsendelse distribuert til Altinn3. forsendelseId={}, bestillingsId={}, konversasjonId={}",
				forsendelse.forsendelseId() , forsendelse.bestillingsId(), konversasjonId);
		administrerForsendelseConsumer.oppdaterForsendelse(oversendt(forsendelse.forsendelseId(), konversasjonId.toString()));
	}

	private void distribuerTilAltinn2(HentForsendelseResponse forsendelse) {
		String konversasjonId = altinn2MeldingService.distribuer(forsendelse);
		log.info("qdist016 Forsendelse distribuert til Altinn2. forsendelseId={}, bestillingsId={}, konversasjonId={}",
				forsendelse.forsendelseId() , forsendelse.bestillingsId(), konversasjonId);
		administrerForsendelseConsumer.oppdaterForsendelse(ekspedert(forsendelse.forsendelseId()));
	}

}
