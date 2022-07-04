package no.nav.dokdistdpv.config.cxf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;

@ConfigurationProperties("altinn.servicecode")
public record ServiceCode(
		@NotBlank String externalServiceCode,
		@NotBlank String externalServiceEditionCode) {
}
