package no.nav.dokdistdpv.sdist007;

import lombok.extern.slf4j.Slf4j;
import no.altinn.correspondenceagencyexternalaec.CorrespondenceStatusResultV3;
import no.altinn.correspondenceagencyexternalaec.CorrespondenceStatusTypeV2;
import no.altinn.correspondenceagencyexternalaec.InsertCorrespondenceV2;
import no.altinn.correspondenceagencyexternalaec.ReceiptExternal;
import no.altinn.correspondenceagencyexternalaec.StatusChangeV2;
import no.nav.dokdistdpv.config.cxf.AltinnClient;
import no.nav.dokdistdpv.consumer.dokarkiv.DokarkivConsumer;
import no.nav.dokdistdpv.consumer.dokarkiv.OppdaterDistribusjonsinfoRequest;
import no.nav.dokdistdpv.consumer.rdist001.AdministrerForsendelseConsumer;
import no.nav.dokdistdpv.consumer.rdist001.OppdaterForsendelserAvstemtInfo.Forsendelse;
import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse;
import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelserResponse;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.springframework.stereotype.Component;

import javax.xml.datatype.XMLGregorianCalendar;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.collect.Lists.partition;
import static java.util.Objects.nonNull;
import static no.altinn.correspondenceagencyexternalaec.CorrespondenceStatusTypeV2.CONFIRMED;
import static no.altinn.correspondenceagencyexternalaec.CorrespondenceStatusTypeV2.READ;
import static no.nav.dokdistdpv.sdist007.altinn.AltinnCorrespondenceECMapper.mapToCorrespondence;

@Slf4j
@Component
public class Sdist007Service {

	public static final int MAX_JOURNALPOSTS_PER_REQUEST = 500;

	private final AdministrerForsendelseConsumer administrerForsendelseConsumer;
	private final DokarkivConsumer dokarkivConsumer;

	private final AltinnClient altinnClient;

	public Sdist007Service(AdministrerForsendelseConsumer administrerForsendelseConsumer,
						   DokarkivConsumer dokarkivConsumer, AltinnClient altinnClient) {
		this.administrerForsendelseConsumer = administrerForsendelseConsumer;
		this.dokarkivConsumer = dokarkivConsumer;
		this.altinnClient = altinnClient;
	}

	@Handler
	public List<Forsendelse> behandleUlesteJournalposter(Exchange exchange) {

		List<String> journalposter = dokarkivConsumer.finnUlesteJournalposter();

		if (journalposter.isEmpty()) {
			log.info("Sdist007 fant ingen uleste journalposter i Joark.");
			return null;
		}

		log.info("Sdist007 fant antall={} uleste journalposter i Joark", journalposter.size());

		List<Forsendelse> ulesteForsendelse = hentUlesteForsendelser(journalposter).stream()
				.map(HentForsendelserResponse::forsendelseListe)
				.flatMap(Collection::stream)
				.map(hentForsendelseResponse -> {
					HentForsendelseResponse.ArkivInformasjon arkivInformasjon = hentForsendelseResponse.arkivInformasjon();
					exchange.setProperty("journalpostId", arkivInformasjon.arkivId());

					Optional<Long> forsendelseId = oppdaterDistribusjonOrSendNotificationToAltinn(hentForsendelseResponse);

					return forsendelseId.map(id -> Forsendelse.builder().forsendelseId(id).build())
							.orElse(null);
				})
				.filter(Objects::nonNull)
				.toList();

		return ulesteForsendelse.isEmpty() ? null : ulesteForsendelse;
	}

	private List<HentForsendelserResponse> hentUlesteForsendelser(List<String> ulesteJournalposter) {
		return partition(ulesteJournalposter, MAX_JOURNALPOSTS_PER_REQUEST).stream()
				.map(administrerForsendelseConsumer::hentForsendelser)
				.toList();
	}

	public Optional<Long> oppdaterDistribusjonOrSendNotificationToAltinn(HentForsendelseResponse hentForsendelseResponse) {
		String journalpostId = hentForsendelseResponse.arkivInformasjon().arkivId();

		Optional<CorrespondenceStatusResultV3> correspondenceStatusResultV3 = altinnClient.hentCorrespondenceStatusResult(hentForsendelseResponse.mottaker().mottakerId(),
				hentForsendelseResponse.konversasjonId());

		if (correspondenceStatusResultV3.isEmpty()) {
			return Optional.empty();
		}

		log.info("Hentet correspondenceStatus for journalpostId={} med correspondenceStatus={}", journalpostId, getCorrespondenceStatus(correspondenceStatusResultV3.get()));

		if (isCorrespondenceResultContainsReadOrConfirmedStatus(correspondenceStatusResultV3.get())) {
			OffsetDateTime statusDate = getLatestReadOrConfirmedStatusDate(correspondenceStatusResultV3.get());
			if (nonNull(statusDate)) {
				dokarkivConsumer.oppdaterDistribusjonsinfo(journalpostId,
						OppdaterDistribusjonsinfoRequest.builder()
								.settStatusEkspedert(false)
								.datoLest(statusDate)
								.build());
			}
			return Optional.empty();

		} else {
			sendNotification(hentForsendelseResponse);
			return Optional.of(hentForsendelseResponse.forsendelseId());
		}
	}

	private void sendNotification(HentForsendelseResponse hentForsendelseResponse) {
		InsertCorrespondenceV2 insertCorrespondenceV2 = mapToCorrespondence(hentForsendelseResponse);
		ReceiptExternal receipt = altinnClient.insertCorrespondence(hentForsendelseResponse.konversasjonId(), insertCorrespondenceV2);
		log.info("journalpostId={} har ikke lest og har sendt påminnelse til Altinn med receiptId={}, receiptStatusCode={}", hentForsendelseResponse.arkivInformasjon().arkivId(),
				receipt.getReceiptId() == null ? receipt.getParentReceiptId() : receipt.getReceiptId(), receipt.getReceiptStatusCode());
	}

	private boolean isCorrespondenceResultContainsReadOrConfirmedStatus(CorrespondenceStatusResultV3 correspondenceStatusResultV3) {
		return correspondenceStatusResultV3.getCorrespondenceStatusInformation().getCorrespondenceStatusDetailsList().getStatusV2()
				.stream()
				.map(statusV2 -> statusV2.getStatusChanges().getStatusChangeV2())
				.flatMap(Collection::stream)
				.anyMatch(statusChangeV2 -> isStatusReadOrConfirmed(statusChangeV2.getStatusType()));
	}

	private OffsetDateTime getLatestReadOrConfirmedStatusDate(CorrespondenceStatusResultV3 correspondenceStatusResultV3) {
		Optional<XMLGregorianCalendar> statusDate = correspondenceStatusResultV3.getCorrespondenceStatusInformation().getCorrespondenceStatusDetailsList().getStatusV2()
				.stream()
				.map(statusV2 -> statusV2.getStatusChanges().getStatusChangeV2())
				.flatMap(Collection::stream)
				.filter(statusV2 -> isStatusReadOrConfirmed(statusV2.getStatusType()))
				.map(StatusChangeV2::getStatusDate)
				.max(XMLGregorianCalendar::compare);
		return statusDate.map(this::convertToOffsetDateTime)
				.orElse(null);
	}

	private List<CorrespondenceStatusTypeV2> getCorrespondenceStatus(CorrespondenceStatusResultV3 correspondenceStatusResultV3) {
		return correspondenceStatusResultV3.getCorrespondenceStatusInformation().getCorrespondenceStatusDetailsList().getStatusV2()
				.stream()
				.map(statusV2 -> statusV2.getStatusChanges().getStatusChangeV2())
				.flatMap(Collection::stream)
				.filter(Objects::nonNull)
				.map(StatusChangeV2::getStatusType)
				.toList();
	}

	private boolean isStatusReadOrConfirmed(CorrespondenceStatusTypeV2 correspondenceStatusTypeV2) {
		return READ.equals(correspondenceStatusTypeV2) || CONFIRMED.equals(correspondenceStatusTypeV2);
	}

	private OffsetDateTime convertToOffsetDateTime(XMLGregorianCalendar xmlGregorianCalendar) {
		return xmlGregorianCalendar.toGregorianCalendar().toZonedDateTime().toOffsetDateTime();
	}
}
