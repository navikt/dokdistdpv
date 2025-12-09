package no.nav.dokdistdpv.qdist016.altinn3.map;

public class ContentNotificationForventedeTekster {
	private static final String VEDTAK_MESSAGE_TITLE = "Vedtak fra Nav";
	private static final String VEDTAK_MESSAGE_SUMMARY = "Vedtak fra Nav";
	private static final String VEDTAK_MESSAGE_BODY = "Du har fått et vedtak som gjelder Oppfølging av ansatt.";
	private static final String VEDTAK_EMAIL_SUBJECT = "Vedtak fra Nav";
	private static final String VEDTAK_EMAIL_BODY = """
			$recipientNumber$ $recipientName$ har mottatt vedtaket «Oppfølging av ansatt» fra Nav i Altinn. \
			For å få tilgang til vedtaket må noen i $recipientName$ få tilgang til tjenesten «Taushetsbelagt post fra Nav» i Altinn. \
			Les mer om tildeling av tilganger og roller på Altinn.\
			""";
	private static final String VEDTAK_SMS_BODY = """
			$recipientNumber$ $recipientName$ har mottatt vedtaket «Oppfølging av ansatt» fra Nav i Altinn. \
			For å få tilgang til vedtaket må noen i $recipientName$ få tilgang til tjenesten «Taushetsbelagt post fra Nav» i Altinn. \
			Les mer om tildeling av tilganger og roller på Altinn.\
			""";
	private static final String VEDTAK_REMINDER_EMAIL_SUBJECT = "Påminnelse: Vedtak fra Nav";
	private static final String VEDTAK_REMINDER_EMAIL_BODY = """
			Dette er en påminnelse om at $recipientNumber$ $recipientName$ har mottatt vedtaket «Oppfølging av ansatt» fra Nav i Altinn. \
			For å få tilgang til vedtaket må noen i $recipientName$ få tilgang til tjenesten «Taushetsbelagt post fra Nav» i Altinn. \
			Les mer om tildeling av tilganger og roller på Altinn.\
			""";
	private static final String VEDTAK_REMINDER_SMS_BODY = """
			Dette er en påminnelse om at $recipientNumber$ $recipientName$ har mottatt vedtaket «Oppfølging av ansatt» fra Nav i Altinn. \
			For å få tilgang til vedtaket må noen i $recipientName$ få tilgang til tjenesten «Taushetsbelagt post fra Nav» i Altinn. \
			Les mer om tildeling av tilganger og roller på Altinn.\
			""";

	static ContentNotificationTekst testVedtak() {
		return ContentNotificationTekst.builder()
				.messageTitle(VEDTAK_MESSAGE_TITLE)
				.messageSummary(VEDTAK_MESSAGE_SUMMARY)
				.messageBody(VEDTAK_MESSAGE_BODY)
				.emailSubject(VEDTAK_EMAIL_SUBJECT)
				.emailBody(VEDTAK_EMAIL_BODY)
				.smsBody(VEDTAK_SMS_BODY)
				.reminderEmailSubject(VEDTAK_REMINDER_EMAIL_SUBJECT)
				.reminderEmailBody(VEDTAK_REMINDER_EMAIL_BODY)
				.reminderSmsBody(VEDTAK_REMINDER_SMS_BODY)
				.build();
	}

	private static final String VIKTIG_MESSAGE_TITLE = "Viktig brev fra Nav";
	private static final String VIKTIG_MESSAGE_SUMMARY = "Viktig brev fra Nav";
	private static final String VIKTIG_MESSAGE_BODY = "Du har fått et brev som du må lese: Oppfølging av ansatt.";
	private static final String VIKTIG_EMAIL_SUBJECT = "Viktig brev fra Nav";
	private static final String VIKTIG_EMAIL_BODY = """
			$recipientNumber$ $recipientName$ har mottatt viktig brev «Oppfølging av ansatt» fra Nav i Altinn. \
			For å få tilgang til brevet må noen i $recipientName$ få tilgang til tjenesten «Taushetsbelagt post fra Nav» i Altinn. \
			Les mer om tildeling av tilganger og roller på Altinn.\
			""";
	private static final String VIKTIG_SMS_BODY = """
			$recipientNumber$ $recipientName$ har mottatt viktig brev «Oppfølging av ansatt» fra Nav i Altinn. \
			For å få tilgang til brevet må noen i $recipientName$ få tilgang til tjenesten «Taushetsbelagt post fra Nav» i Altinn. \
			Les mer om tildeling av tilganger og roller på Altinn.\
			""";
	private static final String VIKTIG_REMINDER_EMAIL_SUBJECT = "Påminnelse: Viktig brev fra Nav";
	private static final String VIKTIG_REMINDER_EMAIL_BODY = """
			Dette er en påminnelse om at $recipientNumber$ $recipientName$ har mottatt viktig brev «Oppfølging av ansatt» fra Nav i Altinn. \
			For å få tilgang til brevet må noen i $recipientName$ få tilgang til tjenesten «Taushetsbelagt post fra Nav» i Altinn. \
			Les mer om tildeling av tilganger og roller på Altinn.\
			""";
	private static final String VIKTIG_REMINDER_SMS_BODY = """
			Dette er en påminnelse om at $recipientNumber$ $recipientName$ har mottatt viktig brev «Oppfølging av ansatt» fra Nav i Altinn. \
			For å få tilgang til brevet må noen i $recipientName$ få tilgang til tjenesten «Taushetsbelagt post fra Nav» i Altinn. \
			Les mer om tildeling av tilganger og roller på Altinn.\
			""";

	static ContentNotificationTekst testViktig() {
		return ContentNotificationTekst.builder()
				.messageTitle(VIKTIG_MESSAGE_TITLE)
				.messageSummary(VIKTIG_MESSAGE_SUMMARY)
				.messageBody(VIKTIG_MESSAGE_BODY)
				.emailSubject(VIKTIG_EMAIL_SUBJECT)
				.emailBody(VIKTIG_EMAIL_BODY)
				.smsBody(VIKTIG_SMS_BODY)
				.reminderEmailSubject(VIKTIG_REMINDER_EMAIL_SUBJECT)
				.reminderEmailBody(VIKTIG_REMINDER_EMAIL_BODY)
				.reminderSmsBody(VIKTIG_REMINDER_SMS_BODY)
				.build();
	}

	private static final String ANNET_MESSAGE_TITLE = "Melding fra Nav";
	private static final String ANNET_MESSAGE_SUMMARY = "Melding fra Nav";
	private static final String ANNET_MESSAGE_BODY = "Du har fått en melding som gjelder Oppfølging av ansatt.";
	private static final String ANNET_EMAIL_SUBJECT = "Melding fra Nav";
	private static final String ANNET_EMAIL_BODY = """
			$recipientNumber$ $recipientName$ har mottatt meldingen «Oppfølging av ansatt» fra Nav i Altinn. \
			For å få tilgang til meldingen må noen i $recipientName$ få tilgang til tjenesten «Taushetsbelagt post fra Nav» i Altinn. \
			Les mer om tildeling av tilganger og roller på Altinn.\
			""";
	private static final String ANNET_SMS_BODY = """
			$recipientNumber$ $recipientName$ har mottatt meldingen «Oppfølging av ansatt» fra Nav i Altinn. \
			For å få tilgang til meldingen må noen i $recipientName$ få tilgang til tjenesten «Taushetsbelagt post fra Nav» i Altinn. \
			Les mer om tildeling av tilganger og roller på Altinn.\
			""";
	private static final String ANNET_REMINDER_EMAIL_SUBJECT = "Påminnelse: Melding fra Nav";
	private static final String ANNET_REMINDER_EMAIL_BODY = """
			Dette er en påminnelse om at $recipientNumber$ $recipientName$ har mottatt meldingen «Oppfølging av ansatt» fra Nav i Altinn. \
			For å få tilgang til meldingen må noen i $recipientName$ få tilgang til tjenesten «Taushetsbelagt post fra Nav» i Altinn. \
			Les mer om tildeling av tilganger og roller på Altinn.\
			""";
	private static final String ANNET_REMINDER_SMS_BODY = """
			Dette er en påminnelse om at $recipientNumber$ $recipientName$ har mottatt meldingen «Oppfølging av ansatt» fra Nav i Altinn. \
			For å få tilgang til meldingen må noen i $recipientName$ få tilgang til tjenesten «Taushetsbelagt post fra Nav» i Altinn. \
			Les mer om tildeling av tilganger og roller på Altinn.\
			""";

	static ContentNotificationTekst testAnnet() {
		return ContentNotificationTekst.builder()
				.messageTitle(ANNET_MESSAGE_TITLE)
				.messageSummary(ANNET_MESSAGE_SUMMARY)
				.messageBody(ANNET_MESSAGE_BODY)
				.emailSubject(ANNET_EMAIL_SUBJECT)
				.emailBody(ANNET_EMAIL_BODY)
				.smsBody(ANNET_SMS_BODY)
				.reminderEmailSubject(ANNET_REMINDER_EMAIL_SUBJECT)
				.reminderEmailBody(ANNET_REMINDER_EMAIL_BODY)
				.reminderSmsBody(ANNET_REMINDER_SMS_BODY)
				.build();
	}
}
