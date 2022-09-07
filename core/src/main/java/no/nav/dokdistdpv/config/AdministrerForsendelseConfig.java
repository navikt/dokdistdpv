package no.nav.dokdistdpv.config;

import lombok.Data;
import no.nav.dokdistdpv.properties.ServiceuserProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.WebClient;

import javax.validation.constraints.NotEmpty;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@ConfigurationProperties("administrerforsendelse")
@Configuration
@Validated
@Data
public class AdministrerForsendelseConfig {

	@NotEmpty
	private String baseUrl;

	@Bean("administrerForsendelseClient")
	public WebClient webClient(WebClient.Builder webClientBuilder,
							   ServiceuserProperties serviceuserProperties) {
		return webClientBuilder
				.clone()
				.baseUrl(baseUrl)
				.defaultHeaders(header -> {
					header.setBasicAuth(serviceuserProperties.getUsername(), serviceuserProperties.getPassword());
					header.setContentType(APPLICATION_JSON);
				})
				.build();
	}
}
