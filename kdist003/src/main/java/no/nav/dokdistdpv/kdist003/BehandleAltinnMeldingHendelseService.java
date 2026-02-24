package no.nav.dokdistdpv.kdist003;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdigdirhendelser.altinn.AltinnEvents;
import no.nav.dokdistdpv.consumer.dokarkiv.DokarkivConsumer;
import no.nav.dokdistdpv.consumer.dokarkiv.OppdaterDistribusjonsinfoRequest;
import no.nav.dokdistdpv.consumer.rdist001.AdministrerForsendelseConsumer;
import no.nav.dokdistdpv.consumer.rdist001.domain.DistribuerTilNyKanalRequest;
import no.nav.dokdistdpv.consumer.rdist001.domain.FinnForsendelseRequest;
import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse;
import no.nav.dokdistdpv.consumer.rdist001.domain.OppdaterForsendelseRequest;
import no.nav.dokdistdpv.kdist003.domain.InternAltinnEvents;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static no.nav.dokdistdpv.consumer.rdist001.domain.FinnForsendelseRequest.Oppslagsnoekkel.KONVERSASJONSID;
import static no.nav.dokdistdpv.kdist003.Kdist003Constants.ALTINN_EVENT_TYPES_MED_INGEN_BEHANDLING;
import static no.nav.dokdistdpv.kdist003.Kdist003Constants.ALTINN_EVENT_TYPES_OPPDATER_LEST_DATO;
import static no.nav.dokdistdpv.kdist003.Kdist003Constants.ALTINN_EVENT_TYPE_OPPDATER_TIL_EKSPEDERT;
import static no.nav.dokdistdpv.kdist003.Kdist003Constants.ALTINN_EVENT_TYPE_SEND_TIL_PRINT;
import static no.nav.dokdistdpv.kdist003.Kdist003Constants.CORRESPONDENCE_PUBLISH_FAILED;
import static no.nav.dokdistdpv.kdist003.Kdist003Constants.FORSENDELSE_STATUS_OVERSENDT;
import static no.nav.dokdistdpv.kdist003.Kdist003Validator.validateAltinnEvent;

@Slf4j
@Component
public class BehandleAltinnMeldingHendelseService {

	public static final String MELDINGSFEIL = "MELDINGSFEIL";
	public static final String VARSLINGSFEIL = "VARSLINGSFEIL";
	public static final String AARSAK_PUBLISERING_FEIL = "Publisering av meldingen feilet";
	public static final String AARSAK_VARSLING_FEIL = "Utsending av varsel feilet";
	public static final String ARKIV_SYSTEM_JOARK = "Joark";

	private final AdministrerForsendelseConsumer administrerForsendelseConsumer;
	private final DokarkivConsumer dokarkivConsumer;

	public BehandleAltinnMeldingHendelseService(
			DokarkivConsumer dokarkivConsumer,
			AdministrerForsendelseConsumer administrerForsendelseConsumer) {
		this.dokarkivConsumer = dokarkivConsumer;
		this.administrerForsendelseConsumer = administrerForsendelseConsumer;
	}

	@KafkaListener(
			topics = "${dokdistdpv.topic.altinn-melding-hendelse}",
			groupId = "dokdistdpv-kdist003")
	public void lesOgBehandleAltinnMelding(ConsumerRecord<String, AltinnEvents> altinnEventsConsumerRecord) {

		try {
			AltinnEvents altinnEvents = altinnEventsConsumerRecord.value();
			log.info("kdist003 mottatt kafka-hendelse med resourceinstance={} og type={}", altinnEvents.resourceinstance(), altinnEvents.type());

			if (altinnEvents == null || ALTINN_EVENT_TYPES_MED_INGEN_BEHANDLING.contains(altinnEvents.type())) {
				log.info("Altinn event med resourceinstance={} og type={} kan ikke behandles", altinnEvents.resourceinstance(), altinnEvents.type());
			}

			InternAltinnEvents internAltinnEvents = MapInternAltinnEvent.map(altinnEvents);

			validateAltinnEvent(internAltinnEvents);
			behandleForsendelse(internAltinnEvents);

		} catch (Exception e) {
			log.error("feilet med parsing av kafka-hendelse til Json - {}", e.getMessage(), e);
		}
	}

	private void behandleForsendelse(InternAltinnEvents internAltinnEvents) {
		HentForsendelseResponse hentForsendelse = hentForsendelse(internAltinnEvents.resourceinstance().toString());

		if (hentForsendelse != null) {
			if (!FORSENDELSE_STATUS_OVERSENDT.equals(hentForsendelse.forsendelseStatus())) {
				log.info("forsendelse med forsendelseId={} og status={} kan ikke behandles", hentForsendelse.forsendelseId(), hentForsendelse.forsendelseStatus());
			} else if (ALTINN_EVENT_TYPE_SEND_TIL_PRINT.contains(internAltinnEvents.type())) {
				administrerForsendelseConsumer.distribuerTilNyKanal(mapDistribuerTilPrint(internAltinnEvents.type(), hentForsendelse.forsendelseId()));
			} else if (ALTINN_EVENT_TYPE_OPPDATER_TIL_EKSPEDERT.contains(internAltinnEvents.type())) {
				administrerForsendelseConsumer.oppdaterForsendelse(
						OppdaterForsendelseRequest.ekspedert(hentForsendelse.forsendelseId()));
			} else if (ALTINN_EVENT_TYPES_OPPDATER_LEST_DATO.contains(internAltinnEvents.type()) && ARKIV_SYSTEM_JOARK.equalsIgnoreCase(hentForsendelse.arkivInformasjon().arkivSystem())) {
				dokarkivConsumer.oppdaterDistribusjonsinfo(hentForsendelse.arkivInformasjon().arkivId(), OppdaterDistribusjonsinfoRequest.builder()
						.settStatusEkspedert(false)
						.datoLest(internAltinnEvents.time())
						.build());
			}
		}
	}

	private HentForsendelseResponse hentForsendelse(String konversasjonId) {
		String forsendelseId = administrerForsendelseConsumer.finnForsendelse(FinnForsendelseRequest.builder()
				.oppslagsnoekkel(KONVERSASJONSID.noekkel)
				.verdi(konversasjonId)
				.build());
		if (forsendelseId == null) {
			return null;
		}
		return administrerForsendelseConsumer.hentForsendelse(forsendelseId);
	}

	private DistribuerTilNyKanalRequest mapDistribuerTilPrint(String eventType, Long forsendelseId) {
		if (CORRESPONDENCE_PUBLISH_FAILED.equals(eventType)) {
			return DistribuerTilNyKanalRequest.builder()
					.forsendelseId(forsendelseId)
					.arsak(MELDINGSFEIL)
					.arsakBeskrivelse(AARSAK_PUBLISERING_FEIL)
					.build();

		}

		return DistribuerTilNyKanalRequest.builder()
				.forsendelseId(forsendelseId)
				.arsak(VARSLINGSFEIL)
				.arsakBeskrivelse(AARSAK_VARSLING_FEIL)
				.build();
	}
}
