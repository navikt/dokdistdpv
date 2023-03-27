package no.nav.dokdistdpv.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@ConfigurationProperties("dokdistdpv")
@Validated
public class DokdistdpvProperties {

	private final Qdist016 qdist016 = new Qdist016();
	private final Endpoints endpoints = new Endpoints();

	@Data
	@Validated
	public static class Qdist016 {
		private boolean autostartup;
	}

	@Data
	@Validated
	public static class Endpoints {
		@NotNull
		private AppEndpoint dokdistadmin;

	}

	@Data
	@Validated
	public static class AppEndpoint {
		@NotEmpty
		private String url;

		@NotEmpty
		private String scope;
	}
}
