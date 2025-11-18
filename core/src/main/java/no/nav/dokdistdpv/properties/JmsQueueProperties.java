package no.nav.dokdistdpv.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@ConfigurationProperties("dokdistdpv.jms")
@Validated
public class JmsQueueProperties {

	@Valid
	private final Broker broker = new Broker();
	@Valid
	private final Queues queues = new Queues();

	@Data
	@Validated
	public static class Broker {
		@NotEmpty
		private String hostname;
		@NotEmpty
		private String name;
		@Positive
		private int port;
		@NotEmpty
		private String channel;
	}

	@Data
	@Validated
	public static class Queues {
		@NotEmpty
		private String qdist016;
		@NotEmpty
		private String qdist016TekniskFeil;
		@NotEmpty
		private String qdist016FunksjonellFeil;
		@NotEmpty
		private String qdist009;
	}
}
