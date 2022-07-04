package no.nav.dokdistdpv.consumer.saf;

import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
@Builder
public class JournalpostQueryResponse {
	@Builder.Default
	List<DokumentInfo> dokumenter = new ArrayList<>();
	List<Error> errors = new ArrayList<>();

	public record DokumentInfo(
			String dokumentInfoId,
			String tittel
	) {
	}

	public record Error(String message) {
	}
}
