package no.nav.dokdistdpv.sdist007;

import no.nav.dokdistdpv.consumer.rdist001.AdministrerForsendelseConsumer;
import no.nav.dokdistdpv.consumer.rdist001.OppdaterForsendelserAvstemtInfo;
import no.nav.dokdistdpv.consumer.rdist001.OppdaterForsendelserAvstemtInfo.Forsendelse;
import org.apache.camel.Handler;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.google.common.collect.Lists.partition;

@Component
public class OppdaterAvstemtInfo {

	public static final int MAX_FORSENDELSER_PER_REQUEST = 500;
	public static final String AVSTEMT_REFERANSE = "sdist007";

	private final AdministrerForsendelseConsumer administrerForsendelseConsumer;

	public OppdaterAvstemtInfo(AdministrerForsendelseConsumer administrerForsendelseConsumer) {
		this.administrerForsendelseConsumer = administrerForsendelseConsumer;
	}

	@Handler
	public void oppdaterForsendelserAvstemtInfo(List<Forsendelse> ulesteForsendelser) {

		partition(ulesteForsendelser, MAX_FORSENDELSER_PER_REQUEST).forEach(forsendelser ->
				administrerForsendelseConsumer.oppdaterForsendelserAvstemtDatoOgReferanse(OppdaterForsendelserAvstemtInfo.builder()
						.forsendelser(forsendelser)
						.avstemtReferanse(AVSTEMT_REFERANSE)
						.build())
		);
	}
}
