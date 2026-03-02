package no.nav.dokdistdpv.consumer.rdist001.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record DistribuerTilNyKanalRequest(
		long forsendelseId,
		String arsak,
		String arsakBeskrivelse) {

	@JsonProperty
	public String kanal() {
		return "PRINT";
	}
}
