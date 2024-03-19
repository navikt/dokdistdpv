package no.nav.dokdistdpv.qdist016;

import lombok.extern.slf4j.Slf4j;
import no.altinn.correspondenceagencyexternalaec.InsertCorrespondenceV2;
import no.nav.dokdistdpv.config.cxf.AltinnClient;
import no.nav.dokdistdpv.consumer.rdist001.AdministrerForsendelseConsumer;
import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse;
import no.nav.dokdistdpv.consumer.rdist001.domain.OppdaterForsendelseRequest;
import no.nav.dokdistdpv.exception.AltinnException;
import no.nav.dokdistdpv.properties.AltinnProperties;
import no.nav.meldinger.virksomhet.dokdistfordeling.qdist008.out.DistribuerTilKanal;
import org.apache.camel.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static java.lang.Long.valueOf;
import static java.util.UUID.randomUUID;
import static no.altinn.correspondenceagencyexternalaec.ReceiptStatusEnum.OK;
import static no.nav.dokdistdpv.config.cxf.mapping.AltinnForsendelseMapper.mapToCorrespondence;
import static no.nav.dokdistdpv.qdist016.Validator.validerForsendelse;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Service
public class Qdist016Service {

	private static final Logger secureLog = LoggerFactory.getLogger("secureLog");
	private static final String FORSENDELSE_STATUS_EKSPEDERT = "EKSPEDERT";

	private final AdministrerForsendelseConsumer administrerForsendelseConsumer;
	private final DokumentService dokumentService;
	private final AltinnClient altinnClient;
	private final AltinnProperties altinnProperties;

	public Qdist016Service(AdministrerForsendelseConsumer administrerForsendelseConsumer,
						   DokumentService dokumentService,
						   AltinnClient altinnClient,
						   AltinnProperties altinnProperties
	) {
		this.administrerForsendelseConsumer = administrerForsendelseConsumer;
		this.dokumentService = dokumentService;
		this.altinnClient = altinnClient;
		this.altinnProperties = altinnProperties;
	}

	@Handler
	public String distribuerForsendelseTilDPV(DistribuerTilKanal distribuerTilKanal) {

		var forsendelseId = distribuerTilKanal.getForsendelseId();
		HentForsendelseResponse forsendelse = administrerForsendelseConsumer.hentForsendelse(forsendelseId);
		validerForsendelse(forsendelseId, forsendelse);

		var konversasjonId = genererKonversasjonId(forsendelseId, forsendelse);
		var dokumenter = dokumentService.hentDokumenter(forsendelse);

		InsertCorrespondenceV2 insertCorrespondenceV2 = mapToCorrespondence(forsendelse, dokumenter,
				altinnProperties.serviceCode(),
				altinnProperties.serviceEditionCode());

		var receipt = altinnClient.insertCorrespondence(konversasjonId, insertCorrespondenceV2);

		if (receipt.getReceiptStatusCode() == OK) {
			log.info("qdist016 Forsendelse distribuert til Altinn med status={} og statusCode={}", receipt.getReceiptTypeName(), receipt.getReceiptStatusCode());
			secureLog.info("Forsendelse distribuert til Altinn med status={} og statusCode={}", receipt.getReceiptTypeName(), receipt.getReceiptStatusCode());

			administrerForsendelseConsumer.oppdaterForsendelse(new OppdaterForsendelseRequest(valueOf(forsendelseId), FORSENDELSE_STATUS_EKSPEDERT, null));
		} else {
			log.error("qdist016 Forsendelse forsøkt distribuert til Altinn feilet med status={} og statusCode={}", receipt.getReceiptTypeName(), receipt.getReceiptStatusCode());
			secureLog.error("Forsendelse forsøkt distribuert til Altinn feilet med status={} og statusCode={}", receipt.getReceiptTypeName(), receipt.getReceiptStatusCode());

			throw new AltinnException("Distribusjon av forsendelse feilet! Status=%s Statuskode=%s text=%s"
					.formatted(receipt.getReceiptTypeName(), receipt.getReceiptStatusCode(), receipt.getReceiptText()));
		}

		return forsendelseId;
	}

	String genererKonversasjonId(String forsendelseId, HentForsendelseResponse forsendelse) {
		var konversasjonId = forsendelse.konversasjonId();

		if (isBlank(konversasjonId)) {
			konversasjonId = randomUUID().toString();
			administrerForsendelseConsumer.oppdaterForsendelse(
					new OppdaterForsendelseRequest(valueOf(forsendelseId), null, konversasjonId));
		}

		return konversasjonId;
	}
}
