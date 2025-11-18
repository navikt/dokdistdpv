package no.nav.dokdistdpv.qdist016;

import lombok.extern.slf4j.Slf4j;
import no.altinn.correspondenceagencyexternalaec.InsertCorrespondenceV2;
import no.nav.dokdistdpv.consumer.altinn2.Altinn2Client;
import no.nav.dokdistdpv.consumer.altinn2.mapping.AltinnDokument;
import no.nav.dokdistdpv.consumer.rdist001.AdministrerForsendelseConsumer;
import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse;
import no.nav.dokdistdpv.exception.AltinnException;
import no.nav.dokdistdpv.properties.AltinnProperties;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.UUID.randomUUID;
import static no.altinn.correspondenceagencyexternalaec.ReceiptStatusEnum.OK;
import static no.nav.dokdistdpv.consumer.altinn2.mapping.Altinn2ForsendelseMapper.mapToCorrespondence;
import static no.nav.dokdistdpv.consumer.rdist001.domain.OppdaterForsendelseRequest.konversasjonId;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Component
public class Altinn2MeldingService {

	private final AdministrerForsendelseConsumer administrerForsendelseConsumer;
	private final DokumentService dokumentService;
	private final Altinn2Client altinn2Client;
	private final AltinnProperties altinnProperties;

	public Altinn2MeldingService(AdministrerForsendelseConsumer administrerForsendelseConsumer,
								 DokumentService dokumentService,
								 Altinn2Client altinn2Client,
								 AltinnProperties altinnProperties) {
		this.administrerForsendelseConsumer = administrerForsendelseConsumer;
		this.dokumentService = dokumentService;
		this.altinn2Client = altinn2Client;
		this.altinnProperties = altinnProperties;
	}

	String distribuer(HentForsendelseResponse forsendelse) {
		long forsendelseId = forsendelse.forsendelseId();
		String konversasjonId = genererKonversasjonId(forsendelseId, forsendelse);
		List<AltinnDokument> dokumenter = dokumentService.hentDokumenter(forsendelse);

		InsertCorrespondenceV2 insertCorrespondenceV2 = mapToCorrespondence(forsendelse, dokumenter,
				altinnProperties.serviceCode(),
				altinnProperties.serviceEditionCode());

		var receipt = altinn2Client.insertCorrespondence(konversasjonId, insertCorrespondenceV2);

		if (receipt.getReceiptStatusCode() != OK) {
			log.error("qdist016 Forsendelse med forsendelseId={}, bestillingsId={} forsøkt distribuert til Altinn2 feilet med status={} og statusCode={}",
					forsendelseId, forsendelse.bestillingsId(),
					receipt.getReceiptTypeName(), receipt.getReceiptStatusCode());
			throw new AltinnException("Distribusjon av forsendelse til Altinn2 feilet! Status=%s, Statuskode=%s, text=%s"
					.formatted(receipt.getReceiptTypeName(), receipt.getReceiptStatusCode(), receipt.getReceiptText()));
		}
		return konversasjonId;
	}

	private String genererKonversasjonId(long forsendelseId, HentForsendelseResponse forsendelse) {
		String konversasjonId = forsendelse.konversasjonId();

		if (isBlank(konversasjonId)) {
			konversasjonId = randomUUID().toString();
			administrerForsendelseConsumer.oppdaterForsendelse(konversasjonId(forsendelseId, konversasjonId));
		}

		return konversasjonId;
	}
}
