package no.nav.dokdistdpv.qdist016.dokument;

public record NavDokument(
		Long arkivDokumentInfoId,
		String dokumentObjektReferanse,
		String tittel,
		int rekkefoelge
) {
	public boolean isArkivertIJoark() {
		return arkivDokumentInfoId != null;
	}
}
