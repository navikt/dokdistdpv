package no.nav.dokdistdpv.consumer.saf;

import no.nav.dokdistdpv.exception.SafGraphQLFunctionalException;
import no.nav.dokdistdpv.exception.SafGraphQLTechnicalException;
import no.nav.dokdistdpv.properties.DokdistdpvProperties;
import no.nav.dokdistdpv.security.AzureToken;
import no.nav.dokdistdpv.security.WebClientAzureAuthentication;
import no.nav.dokdistdpv.utils.MDCOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
public class SafGraphQLConsumer {

	private final WebClient safGraphQLClient;
	private final JournalpostValidator journalpostValidator;

	public SafGraphQLConsumer(DokdistdpvProperties dokdistdpvProperties,
							  AzureToken azureToken,
							  WebClient.Builder webClientBuilder,
							  JournalpostValidator journalpostValidator) {
		this.journalpostValidator = journalpostValidator;
		this.safGraphQLClient = webClientBuilder
				.clone()
				.baseUrl(dokdistdpvProperties.getEndpoints().getSaf().getUrl())
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.filter(new WebClientAzureAuthentication(azureToken, dokdistdpvProperties.getEndpoints().getSaf().getScope()))
				.build();
	}

	public JournalpostQueryResponse hentJournalpost(String journalpostid) {

		GraphQLRequest request = GraphQLRequest.builder()
				.query(JOURNALPOST_QUERY)
				.operationName("journalpost")
				.variables(singletonMap("queryJournalpostId", journalpostid))
				.build();

		JournalpostQueryResponse safJournalpost = safGraphQLClient
				.post()
				.header("Nav-Call-Id", MDCOperations.getCallId())
				.bodyValue(request)
				.retrieve()
				.bodyToMono(JournalpostQueryResponse.class)
				.doOnError(this::handleError)
				.block();

		journalpostValidator.validate(safJournalpost, journalpostid);

		return safJournalpost;
	}

	private void handleError(Throwable error) {
		if (error instanceof WebClientResponseException response && response.getStatusCode().is4xxClientError()) {
			throw new SafGraphQLFunctionalException(
					format("Kall mot SAF (GraphQL) feilet med status=%s, feilmelding=%s",
							response.getStatusCode(),
							response.getMessage()),
					error);
		} else {
			throw new SafGraphQLTechnicalException(
					format("Kall mot SAF (GraphQL) feilet med feilmelding=%s", error.getMessage()),
					error);
		}
	}

	private static final String JOURNALPOST_QUERY = """
			query journalpost($queryJournalpostId: String!) {
				  journalpost(journalpostId: $queryJournalpostId) {
					dokumenter {
					  dokumentInfoId
					  tittel
					  dokumentvarianter {
					  	variantformat
					  	filstoerrelse
					  }
					}
				  }
				}
			""".replaceAll("\n", "").replaceAll("\t", "");
}
