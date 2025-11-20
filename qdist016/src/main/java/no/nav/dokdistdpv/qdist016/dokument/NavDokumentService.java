package no.nav.dokdistdpv.qdist016.dokument;

import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse;
import no.nav.dokdistdpv.consumer.saf.JournalpostQueryResponse;
import no.nav.dokdistdpv.consumer.saf.SafGraphQLConsumer;
import no.nav.dokdistdpv.exception.KunneIkkeFinneDokumentException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.util.stream.Collectors.toMap;
import static no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse.HOVEDDOKUMENT;
import static no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse.VEDLEGG;

@Component
public class NavDokumentService {
	private final SafGraphQLConsumer safConsumer;

	public NavDokumentService(SafGraphQLConsumer safConsumer) {
		this.safConsumer = safConsumer;
	}

	public NavDokumenter hentNavDokumenter(HentForsendelseResponse forsendelse) {
		NavDokument hoveddokument = hentHoveddokument(forsendelse);
		List<NavDokument> vedlegg = hentVedlegg(forsendelse);
		return new NavDokumenter(forsendelse.bestillingsId(), hoveddokument, vedlegg);
	}

	NavDokument hentHoveddokument(HentForsendelseResponse forsendelse) {
		return forsendelse.dokumenter()
				.stream()
				.filter(dokument -> HOVEDDOKUMENT.equals(dokument.tilknyttetSom()))
				.map(dokument -> new NavDokument(Long.valueOf(dokument.arkivDokumentInfoId()),
						dokument.dokumentObjektReferanse(),
						forsendelse.forsendelseTittel(),
						parseInt(dokument.arkivDokumentInfoId())))
				.findFirst()
				.orElseThrow(() -> new KunneIkkeFinneDokumentException("Kunne ikke finne hoveddokument"));
	}

	List<NavDokument> hentVedlegg(HentForsendelseResponse forsendelse) {
		if (forsendelse.isIkkeArkivertIJoark()) {
			var counter = new AtomicInteger(1);

			return forsendelse.dokumenter()
					.stream()
					.filter(dokument -> VEDLEGG.equals(dokument.tilknyttetSom()))
					.map(dokument -> {
						int rekkefoelge = counter.getAndIncrement();
						return new NavDokument(null, dokument.dokumentObjektReferanse(), "Vedlegg " + rekkefoelge, rekkefoelge);
					})
					.toList();
		} else {
			JournalpostQueryResponse response = safConsumer.hentJournalpost(forsendelse.arkivInformasjon().arkivId());
			Map<String, String> dokumentInfoTitler = mapDokumentInfoTitler(response.getData().journalpost().dokumenter());

			return forsendelse.dokumenter()
					.stream()
					.filter(dokument -> VEDLEGG.equals(dokument.tilknyttetSom()))
					.map(vedlegg -> {
						if (dokumentInfoTitler.containsKey(vedlegg.arkivDokumentInfoId())) {
							String key = vedlegg.arkivDokumentInfoId();
							return new NavDokument(Long.valueOf(key), vedlegg.dokumentObjektReferanse(), dokumentInfoTitler.get(key), parseInt(vedlegg.arkivDokumentInfoId()));
						} else {
							throw new KunneIkkeFinneDokumentException(format("DokumentInfoId=%s ikke funnet i journalpost", vedlegg.arkivDokumentInfoId()));
						}
					}).toList();
		}
	}

	private Map<String, String> mapDokumentInfoTitler(List<JournalpostQueryResponse.DokumentInfo> dokumentInfos) {
		return dokumentInfos.stream()
				.collect(toMap(JournalpostQueryResponse.DokumentInfo::dokumentInfoId, JournalpostQueryResponse.DokumentInfo::tittel));
	}

}
