package no.nav.dokdistdpv.kdist003.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.UUID;

@Builder
public record AltinnEventMelding(
		UUID id,
		@NotBlank(message = "resource kan ikke være tom")
		String resource,
		@NotNull(message = "resourceinstance kan ikke være null og må være en gyldig UUID.")
		UUID resourceinstance,
		URI source,
		String specversion,
		@NotBlank(message = "type kan ikke være tom")
		String type,
		String subject,
		@NotBlank(message = "alternativesubject kan ikke være tom")
		@Pattern(regexp = "^/organisation/\\d{9}$", message = "alternativesubject må være på formatet /organisation/{9 siffer}")
		String alternativesubject,
		@NotNull(message = "time kan ikke være null")
		OffsetDateTime time) {
}
