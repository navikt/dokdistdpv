package no.nav.dokdistdpv.sdist007.altinn;

import no.altinn.correspondenceagencyexternalaec.ExternalContentV2;
import no.altinn.correspondenceagencyexternalaec.InsertCorrespondenceV2;
import no.nav.dokdistdpv.consumer.rdist001.domain.DistribusjonsTypeKode;
import no.nav.dokdistdpv.consumer.rdist001.domain.DistribusjonstidspunktKode;
import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.io.ClassPathResource;
import wiremock.org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static no.nav.dokdistdpv.consumer.rdist001.domain.DistribusjonsTypeKode.ANNET;
import static no.nav.dokdistdpv.consumer.rdist001.domain.DistribusjonsTypeKode.VEDTAK;
import static no.nav.dokdistdpv.consumer.rdist001.domain.DistribusjonsTypeKode.VIKTIG;
import static no.nav.dokdistdpv.sdist007.altinn.AltinnCorrespondenceECMapper.mapToCorrespondence;
import static no.nav.dokdistdpv.sdist007.altinn.AltinnMessage.SERVICE_CODE;
import static no.nav.dokdistdpv.sdist007.altinn.AltinnMessage.SERVICE_EDITION_CODE;
import static no.nav.dokdistdpv.utils.AltinnConstant.BREVET;
import static no.nav.dokdistdpv.utils.AltinnConstant.FROM_ADDRESS;
import static no.nav.dokdistdpv.utils.AltinnConstant.LANGUAGE_CODE_BOKMAAL;
import static no.nav.dokdistdpv.utils.AltinnConstant.MELDINGEN;
import static no.nav.dokdistdpv.utils.AltinnConstant.MESSAGE_TITLE_ANNET;
import static no.nav.dokdistdpv.utils.AltinnConstant.MESSAGE_TITLE_VEDTAK;
import static no.nav.dokdistdpv.utils.AltinnConstant.MESSAGE_TITLE_VIKTIG;
import static no.nav.dokdistdpv.utils.AltinnConstant.NOTIFIKASJON_MED_REVARSEL;
import static no.nav.dokdistdpv.utils.AltinnConstant.NOTIFIKASJON_UTEN_REVARSEL;
import static no.nav.dokdistdpv.utils.AltinnConstant.VEDTAKET;
import static org.assertj.core.api.Assertions.assertThat;

class AltinnCorrespondenceECMapperTest {

	public static final Long FORSENDELSE_ID = 111111111L;
	public static final String BESTILLINGS_ID = "bestillingsId";
	public static final String KONVERSASJON_ID = UUID.randomUUID().toString();
	public static final String TEMA = "DAG";
	public static final String FORSENDELSE_STATUS = "EKSPEDERT";
	public static final String MOTTAKER_NAVN = "mottakerNavn";
	public static final String MOTTAKER_ID = "10203040112";
	public static final String ADRESSELINJE_1 = "adresselinje1";
	public static final String ADRESSELINJE_2 = "adresselinje2";
	public static final String ADRESSELINJE_3 = "adresselinje3";
	public static final String POSTNUMMER = "postnummer";
	public static final String POSTSTED = "poststed";
	public static final String LAND_NO = "NO";
	public static final String OBJEKT_REFERANSE_HOVEDDOK = "objektreferanseHoveddok";
	public static final String DOKUMENTTYPE_ID_HOVEDDOK = "dokumenttypeIdHoveddok";
	public static final String TILKNYTTET_SOM_HOVEDDOK = "HOVEDDOKUMENT";

	public static final String OBJEKT_REFERANSE_VEDLEGG1 = "objektreferanseVedlegg1";
	public static final String DOKUMENTTYPE_ID_VEDLEGG1 = "dokumenttypeIdVedlegg1";
	public static final String OBJEKT_REFERANSE_VEDLEGG2 = "objektreferanseVedlegg2";
	public static final String DOKUMENTTYPE_ID_VEDLEGG2 = "dokumenttypeIdVedlegg2";
	public static final String TILKNYTTET_SOM_VEDLEGG = "VEDLEGG";

	public static final String MOTTAKERTYPE_PERSON = "PERSON";

	public static final String JOURNALPOST_ID = "123456789";
	private static final String DEl_TEXT_TOKEN = " er sendt som taushetsbelagt post fra NAV.";

	@ParameterizedTest
	@MethodSource("provideMessageNotifikasjonInput")
	public void shouldMapForsendelseToAltinnCorrespondence(DistribusjonsTypeKode distribusjonsType, String expectedMessageTitle,
														   String expectedMessageBody, String expectedNotificationType, String expectedTextToken) {
		InsertCorrespondenceV2 insertCorrespondenceV2 = mapToCorrespondence(createHentForsendelseResponse(distribusjonsType));

		assertThat(insertCorrespondenceV2.getServiceCode()).isEqualTo(SERVICE_CODE);
		assertThat(insertCorrespondenceV2.getServiceEdition()).isEqualTo(SERVICE_EDITION_CODE);
		assertThat(insertCorrespondenceV2.getReportee()).isEqualTo(MOTTAKER_ID);

		assertContent(expectedMessageTitle, expectedMessageBody, expectedNotificationType, expectedTextToken, insertCorrespondenceV2);

	}

	private void assertContent(String expectedMessageTitle,
							   String expectedMessageBody,
							   String expectedNotificationType,
							   String expectedTextToken,
							   InsertCorrespondenceV2 result) {

		ExternalContentV2 content = result.getContent();
		assertThat(content.getLanguageCode()).isEqualTo(LANGUAGE_CODE_BOKMAAL);
		assertThat(content.getMessageTitle()).isEqualTo(expectedMessageTitle);
		assertThat(content.getMessageBody()).contains(expectedMessageBody);

		result.getNotifications().getNotification()
				.forEach(notification -> {
					assertThat(notification.getLanguageCode()).isEqualTo(LANGUAGE_CODE_BOKMAAL);
					assertThat(notification.getFromAddress()).isEqualTo(FROM_ADDRESS);
					assertThat(notification.getNotificationType()).isEqualTo(expectedNotificationType);
					notification.getTextTokens().getTextToken().stream().filter(Objects::nonNull)
							.forEach(textToken -> {
								assertThat(textToken.getTokenNum()).isEqualTo(1);
								assertThat(textToken.getTokenValue()).contains(expectedTextToken);
							});

				});

	}

	private static Stream<Arguments> provideMessageNotifikasjonInput() throws IOException {
		return Stream.of(
				Arguments.of(VIKTIG, MESSAGE_TITLE_VIKTIG, classpathToString("__files/altinn/altinn_messagebody.html"), NOTIFIKASJON_MED_REVARSEL, BREVET + DEl_TEXT_TOKEN),
				Arguments.of(VEDTAK, MESSAGE_TITLE_VEDTAK, classpathToString("__files/altinn/altinn_messagebody.html"), NOTIFIKASJON_MED_REVARSEL, VEDTAKET + DEl_TEXT_TOKEN),
				Arguments.of(ANNET, MESSAGE_TITLE_ANNET, classpathToString("__files/altinn/altinn_messagebody.html"), NOTIFIKASJON_UTEN_REVARSEL, MELDINGEN + DEl_TEXT_TOKEN)
		);
	}

	public static HentForsendelseResponse createHentForsendelseResponse(DistribusjonsTypeKode distribusjonsType) {
		return new HentForsendelseResponse(FORSENDELSE_ID, BESTILLINGS_ID, KONVERSASJON_ID, FORSENDELSE_STATUS,
				TEMA, FORSENDELSE_STATUS, createMottaker(), createArkivInformasjon(), createPostadresse(), createDokumenter(),
				DistribusjonstidspunktKode.UMIDDELBART, distribusjonsType);
	}

	public static List<HentForsendelseResponse.Dokument> createDokumenter() {
		return List.of(new HentForsendelseResponse.Dokument(TILKNYTTET_SOM_HOVEDDOK, OBJEKT_REFERANSE_HOVEDDOK, DOKUMENTTYPE_ID_HOVEDDOK, DOKUMENTTYPE_ID_HOVEDDOK),
				new HentForsendelseResponse.Dokument(TILKNYTTET_SOM_VEDLEGG, OBJEKT_REFERANSE_VEDLEGG1, DOKUMENTTYPE_ID_VEDLEGG1, DOKUMENTTYPE_ID_VEDLEGG2),
				new HentForsendelseResponse.Dokument(TILKNYTTET_SOM_VEDLEGG, OBJEKT_REFERANSE_VEDLEGG2, DOKUMENTTYPE_ID_VEDLEGG2, DOKUMENTTYPE_ID_VEDLEGG2));
	}

	public static HentForsendelseResponse.Mottaker createMottaker() {
		return new HentForsendelseResponse.Mottaker(MOTTAKER_ID, MOTTAKER_NAVN, MOTTAKERTYPE_PERSON);
	}

	public static HentForsendelseResponse.Postadresse createPostadresse() {
		return new HentForsendelseResponse.Postadresse(ADRESSELINJE_1, ADRESSELINJE_2, ADRESSELINJE_3,
				POSTNUMMER,
				POSTSTED, LAND_NO);
	}

	public static HentForsendelseResponse.ArkivInformasjon createArkivInformasjon() {
		return new HentForsendelseResponse.ArkivInformasjon(JOURNALPOST_ID, "JOARK");
	}

	public static String classpathToString(String classpathResource) throws IOException {
		try {
			InputStream inputStream = new ClassPathResource(classpathResource).getInputStream();
			String message = IOUtils.toString(inputStream, UTF_8);
			IOUtils.closeQuietly(inputStream);
			return message;
		} catch (IOException e) {
			throw new IOException(format("Kunne ikke åpne classpath-ressurs %s", classpathResource), e);
		}
	}
}