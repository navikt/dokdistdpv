package no.nav.dokdistdpv.consumer.rdist001.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record DistribuerTilNyKanalRequest(
		long forsendelseId,
		String arsak,
		String arsakBeskrivelse) {

	public static final String MELDINGSFEIL = "MELDINGSFEIL";
	public static final String VARSLINGSFEIL = "VARSLINGSFEIL";
	public static final String ARSAK_PUBLISERING_FEILET = "Publisering av meldingen feilet";
	public static final String ARSAK_VARSLING_FEILET = "Utsending av varsel feilet";

	@JsonProperty
	public String kanal() {
		return "PRINT";
	}

	public static DistribuerTilNyKanalRequest arsakMeldingsfeil(long forsendelseId) {
		return DistribuerTilNyKanalRequest.builder()
				.forsendelseId(forsendelseId)
				.arsak(MELDINGSFEIL)
				.arsakBeskrivelse(ARSAK_PUBLISERING_FEILET)
				.build();
	}

	public static DistribuerTilNyKanalRequest arsakVarslingsfeil(long forsendelseId) {
		return DistribuerTilNyKanalRequest.builder()
				.forsendelseId(forsendelseId)
				.arsak(VARSLINGSFEIL)
				.arsakBeskrivelse(ARSAK_VARSLING_FEILET)
				.build();
	}
}
