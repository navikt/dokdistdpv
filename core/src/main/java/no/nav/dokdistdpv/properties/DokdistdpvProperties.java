package no.nav.dokdistdpv.properties;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@ConfigurationProperties("dokdistdpv")
@Validated
public class DokdistdpvProperties {

	private final Qdist016 qdist016 = new Qdist016();
	private final Endpoints endpoints = new Endpoints();
	private final Sdist007 sdist007 = new Sdist007();

	@Data
	@Validated
	public static class Qdist016 {
		private boolean autostartup;
		/**
		 * Toggler logging av Altinn request/response payloads i secureLog
		 * Krever medlemskap i AD gruppen 0000-GA-SECURE_LOG_TEAM_DOKUMENTHANDTERING
		 * <p>
		 * Kan konfigureres i vault uten kodeendring:
		 * dokdistdpv_qdist016_altinnlogg=true
		 */
		private boolean altinnlogg = false;
	}

	@Data
	@Validated
	public static class Sdist007 {
		private boolean autostartup;
		private String cronScheduler;
	}

	@Data
	@Validated
	public static class Endpoints {
		@NotNull
		private AppEndpoint dokdistadmin;
		@NotNull
		private AppEndpoint dokarkiv;
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
