package no.nav.dokdistdpv;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.TimeZone;

@ComponentScan
@Configuration
public class CoreConfig {
	@Bean
	Clock clock() {
		return Clock.system(TimeZone.getTimeZone("Europe/Oslo").toZoneId());
	}
}
