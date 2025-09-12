package no.nav.dokdistdpv.consumer.leaderelection;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.InetAddress;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Component
public class LeaderElectionConsumer {

	private static final String ELECTOR_PATH = "ELECTOR_PATH";

	private final WebClient webClient;

	public LeaderElectionConsumer(WebClient webClient) {
		this.webClient = webClient;
	}

	public boolean isLeader() {
		String electorPath = System.getenv(ELECTOR_PATH);
		if (isBlank(electorPath)) {
			log.warn("Kunne ikke bestemme lederpod på grunn av manglende systemvariabel ELECTOR_PATH.");
			return true;
		}

		try {
			String response = webClient.get()
					.uri("http://" + electorPath)
					.retrieve()
					.bodyToMono(JsonNode.class)
					.map(jsonNode -> jsonNode.get("name").asText())
					.block();
			String hostname = InetAddress.getLocalHost().getHostName();
			return hostname.equals(response);
		} catch (Exception e) {
			log.warn("Kunne ikke bestemme lederpod. Feilmelding: {}", e.getMessage(), e);
			return true;
		}
	}
}
