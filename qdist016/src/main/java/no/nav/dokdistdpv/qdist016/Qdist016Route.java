package no.nav.dokdistdpv.qdist016;

import no.nav.dokdistdpv.exception.DokdistdpvFunctionalException;
import no.nav.dokdistdpv.properties.DokdistdpvProperties;
import no.nav.dokdistdpv.qdist016.metrics.Qdist016HeaderProcessor;
import no.nav.dokdistdpv.qdist016.metrics.Qdist016MetricsRoutePolicy;
import no.nav.meldinger.virksomhet.dokdistfordeling.qdist008.out.DistribuerTilKanal;
import org.apache.camel.CamelContext;
import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.springframework.stereotype.Component;

import javax.jms.Queue;
import javax.xml.bind.JAXBContext;

import static no.nav.dokdistdpv.utils.DokdistdpvConstant.PROPERTY_FORSENDELSE_ID;
import static org.apache.camel.LoggingLevel.ERROR;
import static org.apache.camel.LoggingLevel.INFO;
import static org.apache.camel.LoggingLevel.WARN;

@Component
public class Qdist016Route extends RouteBuilder {

	private static final String SERVICE_ID = "qdist016";

	private final Qdist016Service qdist016Service;
	private final Queue qdist016;
	private final Queue qdist016FunksjonellFeil;
	private final Qdist016MetricsRoutePolicy qdist016MetricsRoutePolicy;
	private final DokdistdpvProperties.Qdist016 qdist016Properties;

	public Qdist016Route(CamelContext context,
						 Qdist016Service qdist016Service,
						 Queue qdist016,
						 Queue qdist016FunksjonellFeil,
						 Qdist016MetricsRoutePolicy qdist016MetricsRoutePolicy,
						 DokdistdpvProperties.Qdist016 qdist016Properties) {
		super(context);
		this.qdist016Service = qdist016Service;
		this.qdist016 = qdist016;
		this.qdist016FunksjonellFeil = qdist016FunksjonellFeil;
		this.qdist016MetricsRoutePolicy = qdist016MetricsRoutePolicy;
		this.qdist016Properties = qdist016Properties;
	}

	@Override
	public void configure() throws Exception {

		errorHandler(defaultErrorHandler()
				.maximumRedeliveries(0)
				.log(log)
				.logExhaustedMessageBody(false)
				.logStackTrace(true)
				.loggingLevel(ERROR));

		onException(DokdistdpvFunctionalException.class, IllegalArgumentException.class)
				.handled(true)
				.useOriginalMessage().log(WARN, log, "${exception}; " + "TODO LOG MELDING HER") //TODO
 				.to("jms:" + qdist016FunksjonellFeil.getQueueName());

		from("jms:" + qdist016.getQueueName() + "?transacted=true")
				.autoStartup(qdist016Properties.isAutostartup())
				.routeId(SERVICE_ID)
				.routePolicy(qdist016MetricsRoutePolicy)
				.setExchangePattern(ExchangePattern.InOnly)
				.process(new Qdist016HeaderProcessor())
				.log(INFO, log, "qdist016 har forsendelse med " + logForsendelseId())
				.to("validator:no/nav/meldinger/virksomhet/dokdistfordeling/xsd/qdist008/out/distribuertilkanal.xsd")
				.unmarshal(new JaxbDataFormat(JAXBContext.newInstance(DistribuerTilKanal.class)))
				.bean(qdist016Service)
				.end();
	}

	private static String logForsendelseId() {
		return "forsendelseId=${exchangeProperty." + PROPERTY_FORSENDELSE_ID + "}";
	}

}
