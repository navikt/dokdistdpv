package no.nav.dokdistdpv.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@ConfigurationProperties("dokdistdpv")
@Validated
public class DokdistdpvProperties {

	private final Qdist016 qdist016 = new Qdist016();

	@Data
	@Validated
	public static class Qdist016 {
		private boolean autostartup;
	}
}
