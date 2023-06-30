package no.nav.dokdistdpv.config.cxf;

import no.nav.dokdistdpv.config.cxf.interceptor.BadContextTokenInFaultInterceptor;
import no.nav.dokdistdpv.config.cxf.interceptor.CookiesInInterceptor;
import no.nav.dokdistdpv.config.cxf.interceptor.CookiesOutInterceptor;
import no.nav.dokdistdpv.config.cxf.interceptor.HeaderInterceptor;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BusConfig {
	private static final Logger secureLog = LoggerFactory.getLogger("secureLog");
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
		bus.getInFaultInterceptors().add(new BadContextTokenInFaultInterceptor());

		bus.getInInterceptors().add(new LoggingInInterceptor());
		bus.getOutInterceptors().add(new LoggingOutInterceptor());
		secureLog.info("springBus={}", bus);
		return bus;
	}

}
