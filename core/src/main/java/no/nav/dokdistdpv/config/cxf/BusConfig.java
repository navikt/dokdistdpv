package no.nav.dokdistdpv.config.cxf;

import no.nav.dokdistdpv.config.cxf.interceptor.BadContextTokenInFaultInterceptor;
import no.nav.dokdistdpv.config.cxf.interceptor.CookiesInInterceptor;
import no.nav.dokdistdpv.config.cxf.interceptor.CookiesOutInterceptor;
import no.nav.dokdistdpv.config.cxf.interceptor.HeaderInterceptor;
import no.nav.dokdistdpv.properties.DokdistdpvProperties;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
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
	public Bus springBus(DokdistdpvProperties dokdistdpvProperties) {
		SpringBus bus = new SpringBus();
		bus.getInInterceptors().add(new CookiesInInterceptor());
		bus.getOutInterceptors().add(new CookiesOutInterceptor());
		bus.getOutInterceptors().add(new HeaderInterceptor());

		bus.getInFaultInterceptors().add(new BadContextTokenInFaultInterceptor());

		if (dokdistdpvProperties.getQdist016().isAltinnlogg()) {
			bus.getInInterceptors().add(new LoggingInInterceptor());
			bus.getOutInterceptors().add(new LoggingOutInterceptor());
		}
		return bus;
	}
}
