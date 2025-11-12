package no.nav.dokdistdpv.certificate;

import jakarta.validation.constraints.NotBlank;
import no.nav.dok.validators.Exists;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("nav.virksomhetssertifikat")
public record KeyStoreProperties(@NotBlank @Exists String key,
								 @NotBlank String path,
								 @NotBlank @Exists String credentials) {
}
