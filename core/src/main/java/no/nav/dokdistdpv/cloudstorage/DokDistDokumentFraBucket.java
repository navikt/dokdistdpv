package no.nav.dokdistdpv.cloudstorage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DokDistDokumentFraBucket {
	private byte[] pdf;
	@With
	private String objectName;
}
