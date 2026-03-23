package no.nav.dokdistdpv.kdist003.domain;

import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.UUID;

import static no.nav.dokdistdpv.kdist003.Kdist003Constants.RESOURCE;

@Builder
public record InternAltinnHendelse(
		String resource,
		UUID resourceinstance,
		String type,
		String subject,
		OffsetDateTime time) {

	public static boolean isResourceNavTaushetsbelagtpost(String resource) {
		return RESOURCE.equals(resource);
	}

}


