package no.nav.dokdistdpv.consumer.rdist001.domain;

import lombok.Builder;

import java.util.List;

@Builder
public record HentForsendelseResponse(
		Long forsendelseId,
		String bestillingsId,
		String konversasjonId,
		String forsendelseStatus,
		String tema,
		String forsendelseTittel,
		Mottaker mottaker,
		ArkivInformasjon arkivInformasjon,
		Postadresse postadresse,
		List<Dokument> dokumenter,
		DistribusjonstidspunktKode distribusjonstidspunkt,
		DistribusjonsTypeKode distribusjonstype) {

	public static final String ARKIV_SYSTEM_JOARK = "JOARK";
	public static final String HOVEDDOKUMENT = "HOVEDDOKUMENT";
	public static final String VEDLEGG = "VEDLEGG";

	public boolean isIkkeArkivertIJoark() {
		return arkivInformasjon == null || !ARKIV_SYSTEM_JOARK.equals(arkivInformasjon.arkivSystem);
	}

	public record Mottaker(
			String mottakerId,
			String mottakerNavn,
			String mottakerType
	) {
	}

	public record ArkivInformasjon(
			String arkivSystem,
			String arkivId
	) {
	}

	public record Postadresse(
			String adresselinje1,
			String adresselinje2,
			String adresselinje3,
			String postnummer,
			String poststed,
			String landkode
	) {
	}

	public record Dokument(
			String tilknyttetSom,
			String dokumentObjektReferanse,
			String arkivDokumentInfoId,
			String dokumenttypeId
	) {
	}
}
