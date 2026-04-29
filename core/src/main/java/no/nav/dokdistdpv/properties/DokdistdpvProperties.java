package no.nav.dokdistdpv.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@ConfigurationProperties("dokdistdpv")
@Validated
public class DokdistdpvProperties {

	@Valid
	private final Qdist016 qdist016 = new Qdist016();
	@Valid
	private final Endpoints endpoints = new Endpoints();
	@Valid
	private final Topics topic = new Topics();

	@Data
	public static class Qdist016 {
		private boolean autostartup;
		@Positive
		private int altinn3VedleggZipGrense;
	}

	@Data
	public static class Endpoints {
		@NotNull
		@Valid
		private AppEndpoint dokdistadmin;
		@NotNull
		@Valid
		private AppEndpoint dokarkiv;
		@NotNull
		@Valid
		private AppEndpoint altinn3;
	}

	@Data
	public static class AppEndpoint {
		@NotEmpty
		private String url;

		@NotEmpty
		private String scope;
	}

	@Data
	public static class Topics {
		@NotEmpty
		private String altinnMeldingHendelse;
	}
}
