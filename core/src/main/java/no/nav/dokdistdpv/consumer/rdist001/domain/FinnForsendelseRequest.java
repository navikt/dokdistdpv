package no.nav.dokdistdpv.consumer.rdist001.domain;

import lombok.Builder;

@Builder
public record FinnForsendelseRequest(
		String oppslagsnoekkel,
		String verdi) {

	public enum Oppslagsnoekkel {
		KONVERSASJONSID("konversasjonsId"),
		BESTILLINGSID("bestillingsId"),
		JOURNALPOSTID("journalpostId");

		public final String noekkel;

		Oppslagsnoekkel(String noekkel) {
			this.noekkel = noekkel;
		}
	}
}
