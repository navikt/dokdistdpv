package no.nav.dokdistdpv.qdist016;

import no.nav.dokdistdpv.config.cxf.mapping.AltinnDokument;
import no.nav.dokdistdpv.consumer.rdist001.domain.DistribusjonsTypeKode;
import no.nav.dokdistdpv.consumer.rdist001.domain.DistribusjonstidspunktKode;
import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse;
import no.nav.meldinger.virksomhet.dokdistfordeling.qdist008.out.DistribuerTilKanal;

import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.UUID.randomUUID;
import static no.nav.dokdistdpv.consumer.rdist001.domain.DistribusjonsTypeKode.VEDTAK;
import static no.nav.dokdistdpv.consumer.rdist001.domain.DistribusjonstidspunktKode.KJERNETID;

public class TestUtils {

	public static final String FORSENDELSE_ID = "1234567";
	public static final String BESTILLINGS_ID = randomUUID().toString();
	public static final String KONVERSASJON_ID = "54ebcff5-784e-4503-821b-fadc42361879";
	public static final String FORSENDELSESTATUS = "KLAR_FOR_DIST";
	public static final String BESTILLENDE_FAGSYSTEM = "HOTSAK";
	public static final String TEMA = "HJE";
	public static final String MODUS = "T";
	public static final String FORSENDELSETITTEL = "Vedtak om støtte";
	public static final String BATCH_ID = "1234";
	public static final String DOKUMENT_PROD_APP = "dokprod";
	public static final DistribusjonstidspunktKode DISTRIBUSJONSTIDSPUNKT = KJERNETID;
	public static final DistribusjonsTypeKode DISTRIBUSJONSTYPE = VEDTAK;


	public static HentForsendelseResponse createHentForsendelseResponse() {
		return createHentForsendelseResponse(KONVERSASJON_ID, createArkivinformasjon());
	}

	public static HentForsendelseResponse createHentForsendelseResponseWithKonversasjonId(String konversasjonId) {
		return createHentForsendelseResponse(konversasjonId, createArkivinformasjon());
	}

	public static HentForsendelseResponse createHentForsendelseResponse(String konversasjonId, HentForsendelseResponse.ArkivInformasjon arkivInformasjon) {
		return new HentForsendelseResponse(
				BESTILLINGS_ID,
				konversasjonId,
				BESTILLENDE_FAGSYSTEM,
				MODUS,
				FORSENDELSESTATUS,
				TEMA,
				FORSENDELSETITTEL,
				BATCH_ID,
				DOKUMENT_PROD_APP,
				createMottaker(),
				arkivInformasjon,
				createPostadresse(),
				createDokumenter(),
				DISTRIBUSJONSTIDSPUNKT,
				DISTRIBUSJONSTYPE
		);
	}

	private static HentForsendelseResponse.Mottaker createMottaker() {
		var MOTTAKER_ID = "123456789";
		var MOTTAKERNAVN = "Statoil";
		var MOTTAKERTYPE = "ORGANISASJON";

		return new HentForsendelseResponse.Mottaker(MOTTAKER_ID, MOTTAKERNAVN, MOTTAKERTYPE);
	}

	private static HentForsendelseResponse.ArkivInformasjon createArkivinformasjon() {
		var ARKIVSYSTEM = "Joark";
		var ARKIV_ID = "9988776655";

		return new HentForsendelseResponse.ArkivInformasjon(ARKIVSYSTEM, ARKIV_ID);
	}

	private static HentForsendelseResponse.Postadresse createPostadresse() {
		var ADRESSELINJE_1 = "Joark";
		var ADRESSELINJE_2 = "Joark";
		var ADRESSELINJE_3 = "Joark";
		var POSTNUMMER = "0171";
		var POSTSTED = "Oslo";
		var LANDKODE = "NO";

		return new HentForsendelseResponse.Postadresse(ADRESSELINJE_1, ADRESSELINJE_2, ADRESSELINJE_3, POSTNUMMER, POSTSTED, LANDKODE);
	}

	public static List<HentForsendelseResponse.Dokument> createDokumenter() {
		var TILKNYTTETSOM_1 = "HOVEDDOKUMENT";
		var TILKNYTTETSOM_2 = "VEDLEGG";
		var TILKNYTTETSOM_3 = "VEDLEGG";
		var DOKUMENTOBJEKTREFERANSE_1 = "gcp_bucket_key_1";
		var DOKUMENTOBJEKTREFERANSE_2 = "gcp_bucket_key_2";
		var DOKUMENTOBJEKTREFERANSE_3 = "gcp_bucket_key_3";
		var ARKIVDOKUMENTINFO_ID_1 = "dokid_i_arkivet_1";
		var ARKIVDOKUMENTINFO_ID_2 = "dokid_i_arkivet_2";
		var ARKIVDOKUMENTINFO_ID_3 = "dokid_i_arkivet_3";
		var DOKUMENTTYPE_ID_1 = "dokkat_id_1";
		var DOKUMENTTYPE_ID_2 = "dokkat_id_2";
		var DOKUMENTTYPE_ID_3 = "dokkat_id_3";

		return List.of(
				new HentForsendelseResponse.Dokument(TILKNYTTETSOM_1, DOKUMENTOBJEKTREFERANSE_1, ARKIVDOKUMENTINFO_ID_1, DOKUMENTTYPE_ID_1),
				new HentForsendelseResponse.Dokument(TILKNYTTETSOM_2, DOKUMENTOBJEKTREFERANSE_2, ARKIVDOKUMENTINFO_ID_2, DOKUMENTTYPE_ID_2),
				new HentForsendelseResponse.Dokument(TILKNYTTETSOM_3, DOKUMENTOBJEKTREFERANSE_3, ARKIVDOKUMENTINFO_ID_3, DOKUMENTTYPE_ID_3)
		);
	}

	public static List<AltinnDokument> createAltinnDokumenter() {
		var pdf1 = "PDF for dokument 1".getBytes(UTF_8);
		var pdf2 = "PDF for dokument 2".getBytes(UTF_8);
		var pdf3 = "PDF for dokument 3".getBytes(UTF_8);
		var tittel1 = "Hoveddokument";
		var tittel2 = "Vedlegg 1";
		var tittel3 = "Vedlegg 2";

		return List.of(
				new AltinnDokument(tittel1, pdf1),
				new AltinnDokument(tittel2, pdf2),
				new AltinnDokument(tittel3, pdf3)
		);
	}

	public static DistribuerTilKanal createDistribuerTilKanal() {
		DistribuerTilKanal distribuerTilKanal = new DistribuerTilKanal();
		distribuerTilKanal.setForsendelseId(FORSENDELSE_ID);
		return distribuerTilKanal;
	}
}
