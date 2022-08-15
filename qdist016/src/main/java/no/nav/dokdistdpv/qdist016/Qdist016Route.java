package no.nav.dokdistdpv.qdist016;

import no.nav.dokdistdpv.exception.AltinnException;
import no.nav.dokdistdpv.exception.DokdistdpvFunctionalException;
import no.nav.dokdistdpv.exception.DokdistdpvTechnicalException;
import no.nav.dokdistdpv.properties.DokdistdpvProperties;
import no.nav.dokdistdpv.qdist016.metrics.Qdist016HeaderProcessor;
import no.nav.dokdistdpv.qdist016.metrics.Qdist016MetricsRoutePolicy;
import no.nav.dokdistdpv.utils.MDCRemoveProcessor;
import no.nav.dokdistdpv.utils.MDCSetProcessor;
import no.nav.meldinger.virksomhet.dokdistfordeling.qdist008.out.DistribuerTilKanal;
import org.apache.camel.CamelContext;
import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.springframework.stereotype.Component;

import javax.jms.Queue;
import javax.xml.bind.JAXBContext;

import static java.lang.String.format;
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
	private final Queue qdist016TekniskFeil;
	private final Qdist016MetricsRoutePolicy qdist016MetricsRoutePolicy;
	private final DokdistdpvProperties dokdistdpvProperties;

	public Qdist016Route(CamelContext context,
						 Qdist016Service qdist016Service,
						 Queue qdist016,
						 Queue qdist016FunksjonellFeil,
						 Queue qdist016TekniskFeil,
						 Qdist016MetricsRoutePolicy qdist016MetricsRoutePolicy,
						 DokdistdpvProperties dokdistdpvProperties) {
		super(context);
		this.qdist016Service = qdist016Service;
		this.qdist016 = qdist016;
		this.qdist016FunksjonellFeil = qdist016FunksjonellFeil;
		this.qdist016TekniskFeil = qdist016TekniskFeil;
		this.qdist016MetricsRoutePolicy = qdist016MetricsRoutePolicy;
		this.dokdistdpvProperties = dokdistdpvProperties;
	}

	@Override
	public void configure() throws Exception {

		errorHandler(defaultErrorHandler()
				.maximumRedeliveries(0)
				.log(log)
				.logExhaustedMessageBody(false)
				.logStackTrace(true)
				.loggingLevel(ERROR));

		onException(DokdistdpvTechnicalException.class)
				.handled(true)
				.useOriginalMessage()
				.log(ERROR, log, "${exception}; " + getForsendelseId())
				.to("jms:" + qdist016TekniskFeil.getQueueName());

		onException(DokdistdpvFunctionalException.class, IllegalArgumentException.class)
				.handled(true)
				.useOriginalMessage()
				.log(WARN, log, "${exception}; " + getForsendelseId())
 				.to("jms:" + qdist016FunksjonellFeil.getQueueName());

		onException(AltinnException.class)
				.handled(true)
				.useOriginalMessage()
				.log(WARN, log, format("Feil ved distribusjon av forsendelse med id %s til Altinn. Feilmelding: ${exception.message};", getForsendelseId()))
				.to("jms:" + qdist016FunksjonellFeil.getQueueName());

		from("jms:" + qdist016.getQueueName() + "?transacted=true")
				.autoStartup(dokdistdpvProperties.getQdist016().isAutostartup())
				.routeId(SERVICE_ID)
				.routePolicy(qdist016MetricsRoutePolicy)
				.setExchangePattern(ExchangePattern.InOnly)
				.process(new MDCSetProcessor())
				.process(new Qdist016HeaderProcessor())
				.log(INFO, log, "qdist016 har forsendelse med " + getForsendelseId())
				.to("validator:no/nav/meldinger/virksomhet/dokdistfordeling/xsd/qdist008/out/distribuertilkanal.xsd")
				.unmarshal(new JaxbDataFormat(JAXBContext.newInstance(DistribuerTilKanal.class)))
				.bean(qdist016Service)
				.end()
				.process(new MDCRemoveProcessor());
	}

	private static String getForsendelseId() {
		return format("forsendelseId=${exchangeProperty.%s}", PROPERTY_FORSENDELSE_ID);
	}

}
