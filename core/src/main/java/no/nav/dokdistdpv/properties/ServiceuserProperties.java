package no.nav.dokdistdpv.properties;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

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
