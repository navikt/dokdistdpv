package no.nav.dokdistdpv.config.altinn2;

import no.nav.dokdistdpv.config.altinn2.interceptor.CookiesInInterceptor;
import no.nav.dokdistdpv.config.altinn2.interceptor.CookiesOutInterceptor;
import no.nav.dokdistdpv.config.altinn2.interceptor.HeaderInterceptor;
import no.nav.dokdistdpv.config.altinn2.interceptor.InvalidTokenInterceptor;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BusConfig {
	/**
	 * Initialiserer CFX Bus med nødvendige interceptors og logging.
	 *
	 * @return Bus
	 */
	@Bean
	public Bus springBus() {
		SpringBus bus = new SpringBus();

		bus.getInInterceptors().add(new CookiesInInterceptor());
		bus.getOutInterceptors().add(new CookiesOutInterceptor());
		bus.getOutInterceptors().add(new HeaderInterceptor());

		bus.getInFaultInterceptors().add(new InvalidTokenInterceptor());

		return bus;
	}
}
