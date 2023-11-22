package no.nav.dokdistdpv.sdist007;

import no.nav.dokdistdpv.consumer.leaderelection.LeaderElectionConsumer;
import no.nav.dokdistdpv.exception.AltinnException;
import no.nav.dokdistdpv.exception.DokdistdpvFunctionalException;
import no.nav.dokdistdpv.exception.DokdistdpvTechnicalException;
import no.nav.dokdistdpv.properties.DokdistdpvProperties;
import no.nav.dokdistdpv.utils.MDCSetProcessor;
import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import static java.lang.String.format;
import static no.nav.dokdistdpv.utils.DokdistdpvConstant.PROPERTY_JOURNALPOST_ID;
import static org.apache.camel.LoggingLevel.ERROR;
import static org.apache.camel.LoggingLevel.WARN;

@Component
public class Sdist007Route extends RouteBuilder {

	private static final String SERVICE_ID = "sdist007";
	private final DokdistdpvProperties dokdistdpvProperties;
	private final Sdist007Service sdist007Service;
	private final OppdaterAvstemtInfo oppdaterAvstemtInfo;
	private final LeaderElectionConsumer leaderElection;

	public Sdist007Route(DokdistdpvProperties dokdistdpvProperties,
						 Sdist007Service sdist007Service,
						 OppdaterAvstemtInfo oppdaterAvstemtInfo,
						 LeaderElectionConsumer leaderElection) {
		this.dokdistdpvProperties = dokdistdpvProperties;
		this.sdist007Service = sdist007Service;
		this.oppdaterAvstemtInfo = oppdaterAvstemtInfo;
		this.leaderElection = leaderElection;
	}

	@Override
	public void configure() {

		errorHandler(defaultErrorHandler()
				.maximumRedeliveries(0)
				.log(log)
				.logExhaustedMessageBody(false)
				.logStackTrace(true)
				.loggingLevel(ERROR));

		onException(DokdistdpvTechnicalException.class)
				.handled(true)
				.useOriginalMessage()
				.log(ERROR, log, "${exception}; " + getJournalpostId());

		onException(DokdistdpvFunctionalException.class, IllegalArgumentException.class)
				.handled(true)
				.useOriginalMessage()
				.log(WARN, log, "${exception}; " + getJournalpostId());

		onException(AltinnException.class)
				.handled(true)
				.useOriginalMessage()
				.log(WARN, log, format("Feilet med feilmelding: ${exception.message};", getJournalpostId()));


		from("cron:sdist007?schedule=" + dokdistdpvProperties.getSdist007().getCronScheduler())
			.autoStartup(dokdistdpvProperties.getSdist007().isAutostartup())
			.routeId(SERVICE_ID)
			.setExchangePattern(ExchangePattern.InOnly)
			.process(new MDCSetProcessor())
			.choice()
				.when(method(leaderElection, "isLeader").isEqualTo(true))
				.bean(sdist007Service)
				.choice()
					.when(simple("${body}").isNotNull())
					.bean(oppdaterAvstemtInfo)
				.endChoice()
			.end();

	}

	private static String getJournalpostId() {
		return format("journalpostId=${exchangeProperty.%s}", PROPERTY_JOURNALPOST_ID);
	}

}
