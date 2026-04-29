package no.nav.dokdistdpv.qdist016;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdistdpv.consumer.rdist001.AdministrerForsendelseConsumer;
import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse;
import no.nav.dokdistdpv.qdist016.altinn3.Altinn3MeldingService;
import no.nav.meldinger.virksomhet.dokdistfordeling.qdist008.out.DistribuerTilKanal;
import org.apache.camel.Handler;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static no.nav.dokdistdpv.consumer.rdist001.domain.OppdaterForsendelseRequest.oversendt;
import static no.nav.dokdistdpv.qdist016.Validator.validerForsendelse;
import static no.nav.dokdistdpv.utils.DokdistdpvConstant.MDC_FORSENDELSE_ID;
import static no.nav.dokdistdpv.utils.DokdistdpvConstant.MDC_REQUEST_ID;

@Slf4j
@Service
public class Qdist016Service {
	private static final String QDIST016 = "qdist016";

	private final AdministrerForsendelseConsumer administrerForsendelseConsumer;
	private final Altinn3MeldingService altinn3MeldingService;

	public Qdist016Service(AdministrerForsendelseConsumer administrerForsendelseConsumer,
						   Altinn3MeldingService altinn3MeldingService
	) {
		this.administrerForsendelseConsumer = administrerForsendelseConsumer;
		this.altinn3MeldingService = altinn3MeldingService;
	}

	@SuppressWarnings("unused")
	@Handler
	public String distribuerForsendelseTilDPV(DistribuerTilKanal distribuerTilKanal) {
		String forsendelseId = distribuerTilKanal.getForsendelseId();
		MDC.put(MDC_REQUEST_ID, QDIST016);
		MDC.put(MDC_FORSENDELSE_ID, forsendelseId);
		try {
			HentForsendelseResponse forsendelse = administrerForsendelseConsumer.hentForsendelse(forsendelseId);
			validerForsendelse(forsendelseId, forsendelse);
			distribuerTilAltinn3(forsendelse);
			return forsendelseId;
		} finally {
			MDC.remove(MDC_REQUEST_ID);
			MDC.remove(MDC_FORSENDELSE_ID);
		}
	}

	private void distribuerTilAltinn3(HentForsendelseResponse forsendelse) {
		UUID konversasjonId = altinn3MeldingService.distribuer(forsendelse);
		log.info("qdist016 Forsendelse distribuert til Altinn3. forsendelseId={}, bestillingsId={}, konversasjonId={}",
				forsendelse.forsendelseId(), forsendelse.bestillingsId(), konversasjonId);
		administrerForsendelseConsumer.oppdaterForsendelse(oversendt(forsendelse.forsendelseId(), konversasjonId.toString()));
	}
}
