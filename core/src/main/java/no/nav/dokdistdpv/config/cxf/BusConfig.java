package no.nav.dokdistdpv.config.cxf;

import com.google.api.client.util.LoggingOutputStream;
import no.nav.dokdistdpv.config.cxf.interceptor.BadContextTokenInFaultInterceptor;
import no.nav.dokdistdpv.config.cxf.interceptor.CookiesInInterceptor;
import no.nav.dokdistdpv.config.cxf.interceptor.CookiesOutInterceptor;
import no.nav.dokdistdpv.config.cxf.interceptor.HeaderInterceptor;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
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
		Bus bus = new SpringBus();

		LoggingFeature loggingFeature = new LoggingFeature();
		loggingFeature.setPrettyLogging(true);
		loggingFeature.initialize(bus);
		bus.getFeatures().add(loggingFeature);
		bus.getInInterceptors().add(new CookiesInInterceptor());
		bus.getInInterceptors().add(new LoggingInInterceptor());
		bus.getOutInterceptors().add(new CookiesOutInterceptor());
		bus.getOutInterceptors().add(new LoggingOutInterceptor());
		bus.getOutInterceptors().add(new HeaderInterceptor());
		bus.getInFaultInterceptors().add(new BadContextTokenInFaultInterceptor());

		return bus;
	}

}
