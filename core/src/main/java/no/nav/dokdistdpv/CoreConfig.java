package no.nav.dokdistdpv;

import no.nav.dokdistdpv.properties.DokdistmellomlagerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.TimeZone;

@ComponentScan
@EnableConfigurationProperties(DokdistmellomlagerProperties.class)
@Configuration
public class CoreConfig {
	@Bean
	Clock clock() {
		return Clock.system(TimeZone.getTimeZone("Europe/Oslo").toZoneId());
	}
}
