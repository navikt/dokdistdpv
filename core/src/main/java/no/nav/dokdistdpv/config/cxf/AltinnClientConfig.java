package no.nav.dokdistdpv.config.cxf;

import org.apache.cxf.Bus;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AltinnClientConfig {

    Bus bus;

    public AltinnClientConfig(Bus bus) {
        this.bus = bus;
    }
}
