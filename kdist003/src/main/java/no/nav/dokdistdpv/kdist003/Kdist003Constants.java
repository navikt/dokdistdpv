package no.nav.dokdistdpv.kdist003;

import java.util.Set;

///  [Correspondence events](https://docs.altinn.studio/en/correspondence/getting-started/developer-guides/events/)
public class Kdist003Constants {

	public static final String RESOURCE = "urn:altinn:resource:nav_dokumentdistribusjon_taushetsbelagtpost";
	public static final String MELDING_FEILET_HENDELSESTYPE = "no.altinn.correspondence.correspondencepublishfailed";
	public static final String VARSLING_FEILET_HENDELSESTYPE = "no.altinn.correspondence.correspondencenotificationcreationfailed";

	public static final Set<String> IGNORERTE_HENDELSESTYPER = Set.of(
			"no.altinn.correspondence.attachmentinitialized",
			"no.altinn.correspondence.attachmentuploadprocessing",
			"no.altinn.correspondence.attachmentpublished",
			"no.altinn.correspondence.attachmentuploadfailed",
			"no.altinn.correspondence.attachmentpurged",
			"no.altinn.correspondence.correspondenceinitialized",
			"no.altinn.correspondence.correspondencearchived",
			"no.altinn.correspondence.correspondencepurged",
			"no.altinn.correspondence.correspondencereceiverreserved",
			"no.altinn.correspondence.correspondencereceiverneverread",
			"no.altinn.correspondence.correspondencereceiverneverconfirmed");

	public static final Set<String> OPPDATER_LEST_DATO_HENDELSESTYPER = Set.of(
			"no.altinn.correspondence.correspondencereceiverread",
			"no.altinn.correspondence.correspondencereceiverconfirmed");

	public static final String OPPDATER_TIL_EKSPEDERT_HENDELSESTYPE = "no.altinn.correspondence.correspondencepublished";

	public static final Set<String> SEND_TIL_PRINT_HENDELSESTYPER = Set.of(MELDING_FEILET_HENDELSESTYPE, VARSLING_FEILET_HENDELSESTYPE);

	private Kdist003Constants() {
	}
}
