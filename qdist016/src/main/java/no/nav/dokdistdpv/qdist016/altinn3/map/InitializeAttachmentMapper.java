package no.nav.dokdistdpv.qdist016.altinn3.map;

import no.altinn.services.altinn3.openapi.domain.InitializeAttachmentExt;
import no.nav.dokdistdpv.qdist016.dokument.NavDokument;

import static no.nav.dokdistdpv.consumer.altinn3.Altinn3Constants.NAV_RESOURCE_ID;
import static no.nav.dokdistdpv.qdist016.altinn3.map.NameMapper.mapDisplayName;
import static no.nav.dokdistdpv.qdist016.altinn3.map.NameMapper.mapFileName;

public class InitializeAttachmentMapper {

	public static InitializeAttachmentExt map(NavDokument navDokument, String md5Hex) {
		return InitializeAttachmentExt.builder()
				.fileName(mapFileName(navDokument))
				.displayName(mapDisplayName(navDokument.tittel()))
				.isEncrypted(false)
				.checksum(md5Hex)
				.sendersReference(navDokument.dokumentObjektReferanse())
				.resourceId(NAV_RESOURCE_ID)
				.build();
	}
}
