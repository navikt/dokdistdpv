package no.nav.dokdistdpv.consumer.rdist001;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OppdaterForsendelserAvstemtInfo {
	private String avstemtReferanse;
	private List<Forsendelse> forsendelser;

	@Data
	@Builder
	public static class Forsendelse {
		private Long forsendelseId;
	}
}

