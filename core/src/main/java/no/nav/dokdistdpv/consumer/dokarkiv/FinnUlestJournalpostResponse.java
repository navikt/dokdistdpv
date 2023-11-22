package no.nav.dokdistdpv.consumer.dokarkiv;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class FinnUlestJournalpostResponse {

	private List<String> journalpostListe;

}
