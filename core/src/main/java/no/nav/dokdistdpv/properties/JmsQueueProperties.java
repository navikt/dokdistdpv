package no.nav.dokdistdpv.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;

@Data
@ConfigurationProperties("dokdistdpv.jms")
@Validated
public class JmsQueueProperties {

	private final Broker broker = new Broker();
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
		private String qdist017;
		@NotEmpty
		private String qdist017FunksjonellFeil;
		@NotEmpty
		private String qdist009;
	}
}
