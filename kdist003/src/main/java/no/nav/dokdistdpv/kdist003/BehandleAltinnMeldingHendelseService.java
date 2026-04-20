package no.nav.dokdistdpv.kdist003;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokdigdirhendelser.altinn.AltinnEvent;
import no.nav.dokdistdpv.consumer.dokarkiv.DokarkivConsumer;
import no.nav.dokdistdpv.consumer.dokarkiv.OppdaterDistribusjonsinfoRequest;
import no.nav.dokdistdpv.consumer.rdist001.AdministrerForsendelseConsumer;
import no.nav.dokdistdpv.consumer.rdist001.domain.DistribuerTilNyKanalRequest;
import no.nav.dokdistdpv.consumer.rdist001.domain.FinnForsendelseRequest;
import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse;
import no.nav.dokdistdpv.consumer.rdist001.domain.OppdaterForsendelseRequest;
import no.nav.dokdistdpv.exception.Kdist003JsonProcessingException;
import no.nav.dokdistdpv.kdist003.domain.InternAltinnHendelse;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static no.nav.dokdistdpv.consumer.rdist001.domain.DistribuerTilNyKanalRequest.arsakMeldingsfeil;
import static no.nav.dokdistdpv.consumer.rdist001.domain.DistribuerTilNyKanalRequest.arsakVarslingsfeil;
import static no.nav.dokdistdpv.consumer.rdist001.domain.FinnForsendelseRequest.Oppslagsnoekkel.KONVERSASJONSID;
import static no.nav.dokdistdpv.kdist003.Kdist003Constants.IGNORERTE_HENDELSESTYPER;
import static no.nav.dokdistdpv.kdist003.Kdist003Constants.MELDING_FEILET_HENDELSESTYPE;
import static no.nav.dokdistdpv.kdist003.Kdist003Constants.OPPDATER_LEST_DATO_HENDELSESTYPER;
import static no.nav.dokdistdpv.kdist003.Kdist003Constants.OPPDATER_TIL_EKSPEDERT_HENDELSESTYPE;
import static no.nav.dokdistdpv.kdist003.Kdist003Constants.SEND_TIL_PRINT_HENDELSESTYPER;
import static no.nav.dokdistdpv.kdist003.Kdist003Constants.VARSLING_FEILET_HENDELSESTYPE;
import static no.nav.dokdistdpv.kdist003.Kdist003Validator.validateAltinnEvent;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Component
public class BehandleAltinnMeldingHendelseService {

	public static final String ARKIV_SYSTEM_JOARK = "Joark";
	public static final String FORSENDELSE_STATUS_EKSPEDERT = "EKSPEDERT";

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
	public void lesOgBehandleAltinnMelding(ConsumerRecord<String, AltinnEvent> altinnHendelseConsumerRecord) {

		try {
			AltinnEvent altinnEvent = altinnHendelseConsumerRecord.value();
			log.info("kdist003 mottatt kafka-hendelse med resourceinstance={} og type={}", altinnEvent.resourceinstance(), altinnEvent.type());

			validateAltinnEvent(altinnEvent);

			if (IGNORERTE_HENDELSESTYPER.contains(altinnEvent.type())) {
				loggIngenBehandlingHendelse(altinnEvent);
				return;
			}

			InternAltinnHendelse internAltinnHendelse = MapInternAltinnEvent.map(altinnEvent);
			HentForsendelseResponse hentForsendelse = hentForsendelse(internAltinnHendelse.resourceinstance().toString());

			if (hentForsendelse == null) {
				log.info("Fant ikke forsendelse med konversasjonId={}", internAltinnHendelse.resourceinstance());
				return;
			}

			behandleForsendelse(hentForsendelse, internAltinnHendelse);
		} catch (Exception e) {
			throw new Kdist003JsonProcessingException("kdist003 klarte ikke behandle kafka-hendelse. message=" + e.getMessage(), e);
		}
	}

	private void behandleForsendelse(HentForsendelseResponse hentForsendelse, InternAltinnHendelse internAltinnHendelse) {
		if (SEND_TIL_PRINT_HENDELSESTYPER.contains(internAltinnHendelse.type())) {
			administrerForsendelseConsumer.distribuerTilNyKanal(mapDistribuerTilPrint(internAltinnHendelse.type(), hentForsendelse.forsendelseId()));
		} else if (OPPDATER_TIL_EKSPEDERT_HENDELSESTYPE.contains(internAltinnHendelse.type())) {
			if (FORSENDELSE_STATUS_EKSPEDERT.equals(hentForsendelse.forsendelseStatus())) {
				log.info("forsendelse med forsendelseId={} er allerede ekspedert", hentForsendelse.forsendelseId());
				return;
			}
			administrerForsendelseConsumer.oppdaterForsendelse(
					OppdaterForsendelseRequest.ekspedert(hentForsendelse.forsendelseId()));
		} else if (OPPDATER_LEST_DATO_HENDELSESTYPER.contains(internAltinnHendelse.type()) && ARKIV_SYSTEM_JOARK.equalsIgnoreCase(hentForsendelse.arkivInformasjon().arkivSystem())) {
			dokarkivConsumer.oppdaterDistribusjonsinfo(hentForsendelse.arkivInformasjon().arkivId(), OppdaterDistribusjonsinfoRequest.builder()
					.settStatusEkspedert(false)
					.datoLest(internAltinnHendelse.time())
					.build());
		}
	}

	private HentForsendelseResponse hentForsendelse(String konversasjonId) {
		String forsendelseId = administrerForsendelseConsumer.finnForsendelse(FinnForsendelseRequest.builder()
				.oppslagsnoekkel(KONVERSASJONSID.noekkel)
				.verdi(konversasjonId)
				.build());

		return isBlank(forsendelseId) ? null : administrerForsendelseConsumer.hentForsendelse(forsendelseId);
	}

	private void loggIngenBehandlingHendelse(AltinnEvent altinnEvent) {
		log.info("Altinn hendelse med resourceinstance={} og type={} kan ikke behandles", altinnEvent.resourceinstance(), altinnEvent.type());
	}

	private DistribuerTilNyKanalRequest mapDistribuerTilPrint(String eventType, Long forsendelseId) {
		if (MELDING_FEILET_HENDELSESTYPE.equals(eventType)) {
			return arsakMeldingsfeil(forsendelseId);
		} else if (VARSLING_FEILET_HENDELSESTYPE.equals(eventType)) {
			return arsakVarslingsfeil(forsendelseId);
		} else {
			throw new UnsupportedOperationException("Kan ikke sende til print. eventType=" + eventType);
		}
	}
}
