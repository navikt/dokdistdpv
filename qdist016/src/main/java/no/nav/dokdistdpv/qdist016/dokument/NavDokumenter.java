package no.nav.dokdistdpv.qdist016.dokument;

import lombok.Data;
import no.nav.dokdistdpv.exception.ForsendelseValidationException;
import no.nav.dokdistdpv.exception.KunneIkkeFinneDokumentException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Data
public class NavDokumenter {
	private final String bestillingsId;
	private final NavDokument hoveddokument;
	private final List<NavDokument> vedlegg;
	private final Map<String, NavDokument> lookupRegistry;
	private final int sumVedleggFilstoerrelse;

	public NavDokumenter(String bestillingsId, NavDokument hoveddokument, List<NavDokument> vedlegg) {
		if (bestillingsId == null || bestillingsId.isBlank()) {
			throw new ForsendelseValidationException("Ingen bestillingsId");
		}
		if (hoveddokument == null) {
			throw new KunneIkkeFinneDokumentException("Kunne ikke finne hoveddokument for bestillingsId" + bestillingsId);
		}
		this.bestillingsId = bestillingsId;
		this.hoveddokument = hoveddokument;
		this.vedlegg = vedlegg;
		this.lookupRegistry = populateRegistry();
		this.sumVedleggFilstoerrelse = computeVedleggFilstoerrelse();
	}

	private int computeVedleggFilstoerrelse() {
		return vedlegg.stream().mapToInt(NavDokument::filstoerrelse).sum();
	}

	private Map<String, NavDokument> populateRegistry() {
		Map<String, NavDokument> map = new HashMap<>();
		map.put(hoveddokument.dokumentObjektReferanse(), hoveddokument);
		vedlegg.forEach(navDokument -> {
			if (map.putIfAbsent(navDokument.dokumentObjektReferanse(), navDokument) != null) {
				throw new ForsendelseValidationException("DokumentObjektReferanse finnes allerede. Denne må være unik. " +
						"dokumentObjektReferanse=" + navDokument.dokumentObjektReferanse());
			}
		});
		return map;
	}

	public NavDokument findByDokumentObjektReferanse(String dokumentObjektReferanse) {
		return lookupRegistry.computeIfAbsent(dokumentObjektReferanse, key -> {
			throw new ForsendelseValidationException("Fant ikke dokumentObjektReferanse=" + key);
		});
	}

	/// Hoveddokument og vedlegg
	public List<NavDokument> hoveddokumentOgVedlegg() {
		return Stream.concat(Stream.of(hoveddokument), vedlegg.stream()).toList();
	}
}
