package no.nav.dokdistdpv;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
@EnableRetry
public class CoreConfig {

	public static final ZoneId ZONE_ID_EUROPE_OSLO = ZoneId.of("Europe/Oslo");

	@Bean
	Clock clock() {
		return Clock.system(ZONE_ID_EUROPE_OSLO);
	}
}
