package no.nav.dokdistdpv.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ToString
@ConfigurationProperties("dpv")
@Validated
public class DpvUserProperties {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}
