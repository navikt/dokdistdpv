package no.nav.dokdistdpv.kdist003;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toUnmodifiableSet;

///  [Correspondence events](https://docs.altinn.studio/en/correspondence/getting-started/developer-guides/events/)
public class Kdist003Constants {

	public static final String RESOURCE = "urn:altinn:resource:nav_dokumentdistribusjon_taushetsbelagtpost";
	public static final String MELDING_AAPNET_HENDELSESTYPE = "no.altinn.correspondence.correspondencereceiverread";
	public static final String MELDING_BEKREFTET_HENDELSESTYPE = "no.altinn.correspondence.correspondencereceiverconfirmed";
	public static final String MELDING_PUBLISERT_HENDELSESTYPE = "no.altinn.correspondence.correspondencepublished";
	public static final String MELDING_FEILET_HENDELSESTYPE = "no.altinn.correspondence.correspondencepublishfailed";
	public static final String VARSLING_FEILET_HENDELSESTYPE = "no.altinn.correspondence.correspondencenotificationfailed";
	public static final String OPPRETTELSE_VARSLING_FEILET_HENDELSESTYPE = "no.altinn.correspondence.correspondencenotificationcreationfailed";

	public static final Set<String> OPPDATER_LEST_DATO_HENDELSESTYPER = Set.of(
			MELDING_AAPNET_HENDELSESTYPE,
			MELDING_BEKREFTET_HENDELSESTYPE
	);

	public static final Set<String> OPPDATER_TIL_EKSPEDERT_HENDELSESTYPER = Set.of(
			MELDING_PUBLISERT_HENDELSESTYPE
	);

	public static final Set<String> SEND_TIL_PRINT_HENDELSESTYPER = Set.of(
			MELDING_FEILET_HENDELSESTYPE,
			VARSLING_FEILET_HENDELSESTYPE,
			OPPRETTELSE_VARSLING_FEILET_HENDELSESTYPE
	);

	public static final Set<String> HENDELSESTYPER_SOM_BEHANDLES = Stream.of(
					OPPDATER_LEST_DATO_HENDELSESTYPER,
					OPPDATER_TIL_EKSPEDERT_HENDELSESTYPER,
					SEND_TIL_PRINT_HENDELSESTYPER)
			.flatMap(Set::stream)
			.collect(toUnmodifiableSet());

	private Kdist003Constants() {
	}
}
