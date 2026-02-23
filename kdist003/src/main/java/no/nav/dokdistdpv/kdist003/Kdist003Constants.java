package no.nav.dokdistdpv.kdist003;

import java.util.Set;

public class Kdist003Constants {

	public static final String RESOURCE = "urn:altinn:resource:nav_dokumentdistribusjon_taushetsbelagtpost";
	public static final String ALTERNATIVE_SUBJECT = "/organisation/889640782";
	public static final String FORSENDELSE_STATUS_OVERSENDT = "OVERSENDT";
	public static final String CORRESPONDENCE_PUBLISH_FAILED = "no.altinn.correspondence.correspondencepublishfailed";
	public static final String CORRESPONDENCE_NOTIFICATION_CREATION_FAILED = "no.altinn.correspondence.correspondencenotificationcreationfailed";

	public static final Set<String> ALTINN_EVENT_TYPES_MED_INGEN_BEHANDLING = Set.of(
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

	public static final Set<String> ALTINN_EVENT_TYPES_OPPDATER_LEST_DATO = Set.of(
			"no.altinn.correspondence.correspondencereceiverread",
			"no.altinn.correspondence.correspondencereceiverconfirmed");

	public static final String ALTINN_EVENT_TYPE_OPPDATER_TIL_EKSPEDERT = "no.altinn.correspondence.correspondencepublished";

	public static final Set<String> ALTINN_EVENT_TYPE_SEND_TIL_PRINT = Set.of(
			CORRESPONDENCE_PUBLISH_FAILED,
			CORRESPONDENCE_NOTIFICATION_CREATION_FAILED);

	private Kdist003Constants() {
	}
}
