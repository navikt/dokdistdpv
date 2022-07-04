package no.nav.dokdistdpv.consumer.saf;

import no.nav.dokdistdpv.config.SafGraphQLConfig;
import no.nav.dokdistdpv.exception.SafGraphQLFunctionalException;
import no.nav.dokdistdpv.exception.SafGraphQLTechnicalException;
import no.nav.dokdistdpv.security.AzureToken;
import no.nav.dokdistdpv.security.WebClientAzureAuthentication;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static java.util.Collections.singletonMap;

@Component
public class SafGraphQLConsumer {

	private final WebClient safGraphQLClient;
	private final JournalpostValidator journalpostValidator;

	public SafGraphQLConsumer(AzureToken azureToken,
							  WebClient safGraphQLClient,
							  SafGraphQLConfig safGraphQLConfig,
							  JournalpostValidator journalpostValidator) {
		this.journalpostValidator = journalpostValidator;
		this.safGraphQLClient = safGraphQLClient
				.mutate()
				.filter(new WebClientAzureAuthentication(azureToken, safGraphQLConfig.getScope()))
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
				.bodyValue(BodyInserters.fromValue(request))
				.retrieve()
				.bodyToMono(JournalpostQueryResponse.class)
				.doOnError(this::handleError)
				.block();

		journalpostValidator.validate(safJournalpost, journalpostid);

		return safJournalpost;
	}

	private void handleError(Throwable error) {
		if(error instanceof WebClientResponseException response && ((WebClientResponseException) error).getStatusCode().is4xxClientError()) {
			throw new SafGraphQLFunctionalException(
					String.format("Kall mot SAF (GraphQL) feilet med status=%s, feilmelding=%s",
							response.getRawStatusCode(),
							response.getMessage()),
					error);
		} else {
			throw new SafGraphQLTechnicalException(
					String.format("Kall mot SAF (GraphQL) feilet med feilmelding=%s", error.getMessage()),
					error);
		}
	}


	private static final String JOURNALPOST_QUERY = """
			query journalpost($queryJournalpostId: String!) {
			  journalpost(journalpostId: $queryJournalpostId) {
			    dokumenter {
			      dokumentInfoId
			      tittel
			    }
			  }
			}
			""";
}
