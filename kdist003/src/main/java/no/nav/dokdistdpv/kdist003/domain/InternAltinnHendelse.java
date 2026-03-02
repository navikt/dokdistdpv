package no.nav.dokdistdpv.kdist003.domain;

import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.UUID;

import static no.nav.dokdistdpv.kdist003.Kdist003Constants.ALTERNATIVE_SUBJECT;
import static no.nav.dokdistdpv.kdist003.Kdist003Constants.RESOURCE;

@Builder
public record InternAltinnHendelse(
		String resource,
		UUID resourceinstance,
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


