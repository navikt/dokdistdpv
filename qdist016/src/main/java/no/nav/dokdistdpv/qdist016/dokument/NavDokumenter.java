package no.nav.dokdistdpv.qdist016.dokument;

import no.nav.dokdistdpv.exception.ForsendelseValidationException;
import no.nav.dokdistdpv.exception.KunneIkkeFinneDokumentException;

import java.util.List;
import java.util.stream.Stream;

public record NavDokumenter(
		String bestillingsId,
		NavDokument hoveddokument,
		List<NavDokument> vedlegg
) {
	public NavDokumenter {
		if (bestillingsId == null || bestillingsId.isBlank()) {
			throw new ForsendelseValidationException("Ingen bestillingsId");
		}
		if (hoveddokument == null) {
			throw new KunneIkkeFinneDokumentException("Kunne ikke finne hoveddokument for bestillingsId" + bestillingsId);
		}
	}

	public List<NavDokument> dokumenter() {
		return Stream.concat(Stream.of(hoveddokument), vedlegg.stream()).toList();
	}
}
