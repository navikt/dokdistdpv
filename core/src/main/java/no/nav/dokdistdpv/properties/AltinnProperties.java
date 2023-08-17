package no.nav.dokdistdpv.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;


@Validated
@ConfigurationProperties("altinn")
public record AltinnProperties(@NotNull String endpoint,
							   @NotNull String username,
							   @NotNull @ToString.Exclude String password,
							   @NotNull String userCode,
							   @NotBlank String serviceCode,
							   @NotBlank String serviceEditionCode) {
}
