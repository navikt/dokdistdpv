package no.nav.dokdistdpv.kdist003.domain;

import lombok.Builder;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.UUID;

import static no.nav.dokdistdpv.kdist003.Kdist003Constants.ALTERNATIVE_SUBJECT;
import static no.nav.dokdistdpv.kdist003.Kdist003Constants.RESOURCE;

@Builder
public record InternAltinnEvents(
		UUID id,
		String resource,
		UUID resourceinstance,
		URI source,
		String specversion,
		String type,
		String subject,
		String alternativesubject,
		OffsetDateTime time) {

	public static boolean isSubjectNavOrganisasjon(String subject) {
		return ALTERNATIVE_SUBJECT.equals(subject);
	}

	public static boolean isResourceNavTaushetsbelagtpost(String resource) {
		return RESOURCE.equals(resource);
	}

}


