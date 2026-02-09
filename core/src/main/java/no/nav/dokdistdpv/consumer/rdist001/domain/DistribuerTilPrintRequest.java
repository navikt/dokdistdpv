package no.nav.dokdistdpv.consumer.rdist001.domain;

import lombok.Builder;

@Builder
public record DistribuerTilPrintRequest(
		Long forsendelseId,
		String kanal,
		String arsak,
		String arsakBeskrivelse) {
}
