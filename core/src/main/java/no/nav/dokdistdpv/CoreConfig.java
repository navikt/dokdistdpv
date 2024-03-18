package no.nav.dokdistdpv;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

import java.time.Clock;
import java.util.TimeZone;

@Configuration
@EnableRetry
public class CoreConfig {

	@Bean
	Clock clock() {
		return Clock.system(TimeZone.getTimeZone("Europe/Oslo").toZoneId());
	}
}
