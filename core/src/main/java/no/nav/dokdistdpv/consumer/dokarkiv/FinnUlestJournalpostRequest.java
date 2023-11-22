package no.nav.dokdistdpv.consumer.dokarkiv;

import java.time.LocalDateTime;

public record FinnUlestJournalpostRequest(String utsendingsKanal,
										  LocalDateTime ekspedertFra,
										  LocalDateTime ekspedertTil) {
}
