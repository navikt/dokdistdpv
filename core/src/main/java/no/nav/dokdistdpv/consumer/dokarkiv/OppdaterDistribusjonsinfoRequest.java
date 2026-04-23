package no.nav.dokdistdpv.consumer.dokarkiv;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class OppdaterDistribusjonsinfoRequest {
	private boolean settStatusEkspedert;
	private OffsetDateTime datoLest;
	private boolean tilbakestillJournalpost;
}
