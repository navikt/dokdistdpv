package no.nav.dokdistdpv.qdist016;

import lombok.extern.slf4j.Slf4j;
import no.altinn.correspondenceagencyexternalaec.InsertCorrespondenceV2;
import no.nav.dokdistdpv.consumer.altinn2.Altinn2Client;
import no.nav.dokdistdpv.consumer.rdist001.AdministrerForsendelseConsumer;
import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse;
import no.nav.dokdistdpv.consumer.rdist001.domain.OppdaterForsendelseRequest;
import no.nav.dokdistdpv.exception.AltinnException;
import no.nav.dokdistdpv.properties.AltinnProperties;
import no.nav.meldinger.virksomhet.dokdistfordeling.qdist008.out.DistribuerTilKanal;
import org.apache.camel.Handler;
import org.springframework.stereotype.Service;

import static java.lang.Long.valueOf;
import static java.util.UUID.randomUUID;
import static no.altinn.correspondenceagencyexternalaec.ReceiptStatusEnum.OK;
import static no.nav.dokdistdpv.consumer.altinn2.mapping.Altinn2ForsendelseMapper.mapToCorrespondence;
import static no.nav.dokdistdpv.qdist016.Validator.validerForsendelse;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Service
public class Qdist016Service {

	private static final String FORSENDELSE_STATUS_EKSPEDERT = "EKSPEDERT";

	private final AdministrerForsendelseConsumer administrerForsendelseConsumer;
	private final DokumentService dokumentService;
	private final Altinn2Client altinn2Client;
	private final AltinnProperties altinnProperties;

	public Qdist016Service(AdministrerForsendelseConsumer administrerForsendelseConsumer,
						   DokumentService dokumentService,
						   Altinn2Client altinn2Client,
						   AltinnProperties altinnProperties
	) {
		this.administrerForsendelseConsumer = administrerForsendelseConsumer;
		this.dokumentService = dokumentService;
		this.altinn2Client = altinn2Client;
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

		var receipt = altinn2Client.insertCorrespondence(konversasjonId, insertCorrespondenceV2);

		if (receipt.getReceiptStatusCode() == OK) {
			log.info("qdist016 Forsendelse distribuert til Altinn med status={} og statusCode={}", receipt.getReceiptTypeName(), receipt.getReceiptStatusCode());

			administrerForsendelseConsumer.oppdaterForsendelse(new OppdaterForsendelseRequest(valueOf(forsendelseId), FORSENDELSE_STATUS_EKSPEDERT, null));
		} else {
			log.error("qdist016 Forsendelse forsøkt distribuert til Altinn feilet med status={} og statusCode={}", receipt.getReceiptTypeName(), receipt.getReceiptStatusCode());

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
