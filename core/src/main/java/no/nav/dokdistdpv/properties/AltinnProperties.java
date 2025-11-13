package no.nav.dokdistdpv.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;


@Validated
@ConfigurationProperties("altinn")
public record AltinnProperties(@NotBlank String endpoint,
							   @NotBlank String username,
							   @NotBlank @ToString.Exclude String password,
							   @NotBlank String userCode,
							   @NotBlank String serviceCode,
							   @NotBlank String serviceEditionCode) {
}
