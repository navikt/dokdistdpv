package no.nav.dokdistdpv.consumer.saf;

import no.nav.dokdistdpv.exception.JournalpostValidationException;
import no.nav.dokdistdpv.exception.SafGraphQLFunctionalException;
import org.springframework.stereotype.Component;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class JournalpostValidator {

	public void validate(JournalpostQueryResponse journalpostQueryResponse, String journalpostid) {
		if (journalpostQueryResponse == null) {
			throw new SafGraphQLFunctionalException(
					format("Kall mot SAF (GraphQL) returnerte null for journalpostId=%s",
							journalpostid));
		}

		if (journalpostQueryResponse.getErrors().size() > 0) {
			throw new SafGraphQLFunctionalException(
					format("Kall mot SAF (GraphQL) feilet for journalpostId=%s, med feilmelding(er)=%s",
							journalpostid,
							journalpostQueryResponse.getErrors().stream()
									.map(JournalpostQueryResponse.Error::message)
									.toList()));
		}

		journalpostQueryResponse.getDokumenter().forEach(dokumentInfo -> validateDokument(dokumentInfo, journalpostid));
	}

	private void validateDokument(JournalpostQueryResponse.DokumentInfo dokumentInfo, String journalpostId) {

		if (isBlank(dokumentInfo.tittel())) {
			throw new JournalpostValidationException(
					format("Feltet tittel kan ikke være null eller tomt i journalpost-respons fra SAF. journalpostId=%s, dokumentInfoId=%s",
							journalpostId,
							dokumentInfo.dokumentInfoId()));
		}

		if (isBlank(dokumentInfo.dokumentInfoId())) {
			throw new JournalpostValidationException(
					format("Feltet dokumentInfoId kan ikke være null eller tomt i journalpost-respons fra SAF. journalpostId=%s",
							journalpostId));
		}
	}
}
