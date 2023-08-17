package no.nav.dokdistdpv.properties;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@ConfigurationProperties("dokdistdpv.serviceuser")
@Validated
public class ServiceuserProperties {
	@NotEmpty
	private String username;

	@NotEmpty
	@ToString.Exclude
	private String password;
}
