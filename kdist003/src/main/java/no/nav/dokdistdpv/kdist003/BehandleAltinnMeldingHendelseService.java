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
import no.nav.dokdistdpv.exception.KanIkkeDistribuereTilNyKanalException;
import no.nav.dokdistdpv.exception.Kdist003Exception;
import no.nav.dokdistdpv.kdist003.domain.InternAltinnHendelse;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static no.nav.dokdistdpv.consumer.rdist001.domain.DistribuerTilNyKanalRequest.arsakMeldingsfeil;
import static no.nav.dokdistdpv.consumer.rdist001.domain.DistribuerTilNyKanalRequest.arsakVarslingsfeil;
import static no.nav.dokdistdpv.consumer.rdist001.domain.FinnForsendelseRequest.Oppslagsnoekkel.KONVERSASJONSID;
import static no.nav.dokdistdpv.kdist003.Kdist003Constants.ALL_VARSLING_FEILET_HENDELSESTYPE;
import static no.nav.dokdistdpv.kdist003.Kdist003Constants.HENDELSESTYPER_SOM_BEHANDLES;
import static no.nav.dokdistdpv.kdist003.Kdist003Constants.MELDING_FEILET_HENDELSESTYPE;
import static no.nav.dokdistdpv.kdist003.Kdist003Constants.OPPDATER_LEST_DATO_HENDELSESTYPER;
import static no.nav.dokdistdpv.kdist003.Kdist003Constants.OPPDATER_TIL_EKSPEDERT_HENDELSESTYPER;
import static no.nav.dokdistdpv.kdist003.Kdist003Constants.OPPRETTELSE_VARSLING_FEILET_HENDELSESTYPE;
import static no.nav.dokdistdpv.kdist003.Kdist003Constants.SEND_TIL_PRINT_HENDELSESTYPER;
import static no.nav.dokdistdpv.kdist003.Kdist003Validator.validateAltinnEvent;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Component
public class BehandleAltinnMeldingHendelseService {

	public static final String ARKIV_SYSTEM_JOARK = "Joark";
	public static final String FORSENDELSE_STATUS_EKSPEDERT = "EKSPEDERT";
	public static final String FORSENDELSE_STATUS_FEILET = "FEILET";

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
			log.info("kdist003 har mottatt hendelse med id={}, resourceinstance={}, type={}",
					altinnEvent.id(), altinnEvent.resourceinstance(), altinnEvent.type());

			validateAltinnEvent(altinnEvent);

			if (HENDELSESTYPER_SOM_BEHANDLES.contains(altinnEvent.type())) {
				InternAltinnHendelse internAltinnHendelse = MapInternAltinnEvent.map(altinnEvent);
				behandleHendelse(internAltinnHendelse);
			} else {
				loggIngenBehandling(altinnEvent);
			}
		} catch(KanIkkeDistribuereTilNyKanalException e) {
			log.info("Kan ikke distribuere til ny kanal. Avslutter behandling av hendelsen", e);
		} catch (Exception e) {
			String message = "kdist003 klarte ikke behandle hendelse. message=" + e.getMessage();
			log.error(message, e);
			throw new Kdist003Exception(message, e);
		}
	}

	private void behandleHendelse(InternAltinnHendelse internAltinnHendelse) {
		HentForsendelseResponse hentForsendelse = hentForsendelse(internAltinnHendelse.resourceinstance().toString());

		if (hentForsendelse == null) {
			log.info("Fant ikke forsendelse med konversasjonId={}", internAltinnHendelse.resourceinstance());
			return;
		}

		behandleForsendelse(hentForsendelse, internAltinnHendelse);
	}

	private void behandleForsendelse(HentForsendelseResponse hentForsendelse, InternAltinnHendelse internAltinnHendelse) {
		if (SEND_TIL_PRINT_HENDELSESTYPER.contains(internAltinnHendelse.type())) {
			if (FORSENDELSE_STATUS_FEILET.equals(hentForsendelse.forsendelseStatus())) {
				log.info("forsendelse med forsendelseId={} er satt til feilet. Sender ikke denne til ny kanal", hentForsendelse.forsendelseId());
				return;
			}
			dokarkivConsumer.oppdaterDistribusjonsinfo(hentForsendelse.arkivInformasjon().arkivId(), OppdaterDistribusjonsinfoRequest.builder()
					.tilbakestillJournalpost(true)
					.build());
			administrerForsendelseConsumer.distribuerTilNyKanal(mapDistribuerTilPrint(internAltinnHendelse.type(), hentForsendelse.forsendelseId()));
		} else if (OPPDATER_TIL_EKSPEDERT_HENDELSESTYPER.contains(internAltinnHendelse.type())) {
			if (FORSENDELSE_STATUS_EKSPEDERT.equals(hentForsendelse.forsendelseStatus())) {
				log.info("forsendelse med forsendelseId={} er allerede ekspedert", hentForsendelse.forsendelseId());
				return;
			}
			if (FORSENDELSE_STATUS_FEILET.equals(hentForsendelse.forsendelseStatus())) {
				log.info("forsendelse med forsendelseId={} er satt til feilet. Oppdaterer ikke til ekspedert", hentForsendelse.forsendelseId());
				return;
			}
			administrerForsendelseConsumer.oppdaterForsendelse(
					OppdaterForsendelseRequest.ekspedert(hentForsendelse.forsendelseId()));
		} else if (OPPDATER_LEST_DATO_HENDELSESTYPER.contains(internAltinnHendelse.type()) && ARKIV_SYSTEM_JOARK.equalsIgnoreCase(hentForsendelse.arkivInformasjon().arkivSystem())) {
			dokarkivConsumer.oppdaterDistribusjonsinfo(hentForsendelse.arkivInformasjon().arkivId(), OppdaterDistribusjonsinfoRequest.builder()
					.settStatusEkspedert(!FORSENDELSE_STATUS_EKSPEDERT.equals(hentForsendelse.forsendelseStatus()))
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

	private void loggIngenBehandling(AltinnEvent altinnEvent) {
		log.info("Hendelse med resourceinstance={}, type={} har ingen behandling", altinnEvent.resourceinstance(), altinnEvent.type());
	}

	private DistribuerTilNyKanalRequest mapDistribuerTilPrint(String eventType, Long forsendelseId) {
		return switch (eventType) {
			case MELDING_FEILET_HENDELSESTYPE -> arsakMeldingsfeil(forsendelseId);
			case ALL_VARSLING_FEILET_HENDELSESTYPE, OPPRETTELSE_VARSLING_FEILET_HENDELSESTYPE ->
					arsakVarslingsfeil(forsendelseId);
			case null, default ->
					throw new UnsupportedOperationException("Kan ikke sende til print. eventType=" + eventType);
		};
	}
}
