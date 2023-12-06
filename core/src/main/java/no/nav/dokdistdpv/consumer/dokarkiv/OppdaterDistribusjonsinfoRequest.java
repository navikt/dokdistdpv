package no.nav.dokdistdpv.consumer.dokarkiv;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class OppdaterDistribusjonsinfoRequest {
	private Boolean settStatusEkspedert;
	private OffsetDateTime datoLest;
}
