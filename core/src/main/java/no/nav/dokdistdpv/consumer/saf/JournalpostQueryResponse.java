package no.nav.dokdistdpv.consumer.saf;

import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
@Builder
public class JournalpostQueryResponse {

	Data data;
	List<Error> errors = new ArrayList<>();

	public record Data(
			Journalpost journalpost
	) {
	}

	public record Journalpost(
			List<DokumentInfo> dokumenter
	) {
	}

	public record DokumentInfo(
			String dokumentInfoId,
			String tittel,
			List<DokumentVariant> dokumentvarianter
	) {
	}

	public record DokumentVariant(
			String variantformat,
			Integer filstoerrelse
	) {
	}

	public record Error(String message) {
	}
}
