package no.nav.dokdistdpv.qdist016.altinn3.map;

import no.altinn.services.altinn3.openapi.domain.InitializeAttachmentExt;
import no.nav.dokdistdpv.qdist016.dokument.NavDokument;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static no.nav.dokdistdpv.consumer.altinn3.Altinn3Constants.NAV_RESOURCE_ID;
import static org.assertj.core.api.Assertions.assertThat;

class InitializeAttachmentMapperTest {

	@Test
	void shouldMap() {
		String dokumentObjektReferanse = UUID.randomUUID().toString();
		NavDokument navDokument = new NavDokument(1000L, dokumentObjektReferanse, "Søknad om foreldrepenger", 1, 0);
		String md5Hex = DigestUtils.md5Hex("Hei".getBytes());
		InitializeAttachmentExt initializeAttachmentExt = InitializeAttachmentMapper.map(navDokument, md5Hex);

		assertThat(initializeAttachmentExt.getFileName()).isEqualTo("1000_Søknad om foreldrepenger.pdf");
		assertThat(initializeAttachmentExt.getDisplayName()).isEqualTo("Søknad om foreldrepenger");
		assertThat(initializeAttachmentExt.getIsEncrypted()).isFalse();
		assertThat(initializeAttachmentExt.getChecksum()).isEqualTo(md5Hex);
		assertThat(initializeAttachmentExt.getSendersReference()).isEqualTo(dokumentObjektReferanse);
		assertThat(initializeAttachmentExt.getResourceId()).isEqualTo(NAV_RESOURCE_ID);
	}
}