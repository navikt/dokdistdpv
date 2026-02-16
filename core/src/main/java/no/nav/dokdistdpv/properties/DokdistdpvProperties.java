package no.nav.dokdistdpv.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
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
	private final Sdist007 sdist007 = new Sdist007();
	@Valid
	private final Topics topic = new Topics();

	@Data
	public static class Qdist016 {
		private boolean autostartup;
		private boolean altinn3;
		@Positive
		private int altinn3VedleggZipGrense;
	}

	@Data
	public static class Sdist007 {
		private boolean autostartup;
		@NotEmpty
		private String cronScheduler;
		@PositiveOrZero
		private int fraAntallEkspedertDagerTilbake;
		@PositiveOrZero
		private int tilAntallEkspedertDagerTilbake;
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
