package no.nav.dokdistdpv.qdist016.altinn3.map;

import no.nav.dokdistdpv.consumer.altinn.map.AttachmentNameMapper;
import no.nav.dokdistdpv.qdist016.dokument.NavDokument;

public final class InitializeAttachmentNameMapper {

	public static String mapFileName(NavDokument navDokument) {
		if (navDokument.isArkivertIJoark()) {
			return AttachmentNameMapper.mapFileName(navDokument.arkivDokumentInfoId(), navDokument.tittel());
		}
		return AttachmentNameMapper.mapFileName(navDokument.tittel());
	}
}
