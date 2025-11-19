package no.nav.dokdistdpv.qdist016.altinn2;

import no.nav.dokdistdpv.cloudstorage.EncryptedBucketStorage;
import no.nav.dokdistdpv.consumer.altinn2.mapping.AltinnDokument;
import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse;
import no.nav.dokdistdpv.consumer.saf.SafGraphQLConsumer;
import no.nav.dokdistdpv.exception.KunneIkkeFinneDokumentException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Collections.singletonList;

@Service
public class DokumentService {

	private final String HOVEDDOKUMENT = "HOVEDDOKUMENT";
	private final String VEDLEGG = "VEDLEGG";

	private final EncryptedBucketStorage storage;
	private final SafGraphQLConsumer safConsumer;

	public DokumentService(EncryptedBucketStorage storage, SafGraphQLConsumer safConsumer) {
		this.storage = storage;
		this.safConsumer = safConsumer;
	}

	List<AltinnDokument> hentDokumenter(HentForsendelseResponse forsendelse) {
		var hoveddokument = singletonList(hentHoveddokument(forsendelse));
		var vedlegg = hentVedlegg(forsendelse);

		return Stream.concat(hoveddokument.stream(), vedlegg.stream()).toList();
	}

	AltinnDokument hentHoveddokument(HentForsendelseResponse forsendelse) {
		return forsendelse.dokumenter()
				.stream()
				.filter(dokument -> HOVEDDOKUMENT.equals(dokument.tilknyttetSom()))
				.map(hoveddokument -> storage.downloadObject(hoveddokument.dokumentObjektReferanse(), forsendelse.bestillingsId()))
				.filter(Objects::nonNull)
				.map(nedlastetHoveddokument -> new AltinnDokument(forsendelse.forsendelseTittel(), nedlastetHoveddokument.getPdf()))
				.findFirst()
				.orElseThrow(() -> new KunneIkkeFinneDokumentException("Kunne ikke finne hoveddokument"));
	}

	List<AltinnDokument> hentVedlegg(HentForsendelseResponse forsendelse) {
		if (forsendelse.isIkkeArkivertIJoark()) {
			var counter = new AtomicInteger(1);

			return forsendelse.dokumenter()
					.stream()
					.filter(dokument -> VEDLEGG.equals(dokument.tilknyttetSom()))
					.map(vedlegg -> storage.downloadObject(vedlegg.dokumentObjektReferanse(), forsendelse.bestillingsId()))
					.filter(Objects::nonNull)
					.map(nedlastetVedlegg -> new AltinnDokument("Vedlegg " + counter.getAndIncrement(), nedlastetVedlegg.getPdf()))
					.toList();
		} else {
			var response = safConsumer.hentJournalpost(forsendelse.arkivInformasjon().arkivId());

			return forsendelse.dokumenter()
					.stream()
					.filter(dokument -> VEDLEGG.equals(dokument.tilknyttetSom()))
					.map(vedlegg -> {
						var pdf = storage.downloadObject(vedlegg.dokumentObjektReferanse(), forsendelse.bestillingsId()).getPdf();
						var tittel = response.getData().journalpost().dokumenter().stream()
								.filter(dokumentInfo -> dokumentInfo.dokumentInfoId().equals(vedlegg.arkivDokumentInfoId()))
								.findAny()
								.orElseThrow(() -> new KunneIkkeFinneDokumentException(
										format("DokumentInfoId=%s ikke funnet i journalpost", vedlegg.arkivDokumentInfoId())))
								.tittel();
						return new AltinnDokument(tittel, pdf);
					}).toList();
		}
	}

}
