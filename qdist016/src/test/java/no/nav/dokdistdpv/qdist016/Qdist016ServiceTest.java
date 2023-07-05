package no.nav.dokdistdpv.qdist016;

import no.altinn.correspondenceagencyexternalaec.ReceiptExternal;
import no.altinn.correspondenceagencyexternalaec.ReceiptStatusEnum;
import no.nav.dokdistdpv.config.cxf.AltinnClient;
import no.nav.dokdistdpv.config.cxf.mapping.AltinnDokument;
import no.nav.dokdistdpv.consumer.rdist001.AdministrerForsendelseConsumer;
import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse;
import no.nav.dokdistdpv.consumer.rdist001.domain.OppdaterForsendelseRequest;
import no.nav.dokdistdpv.exception.AltinnException;
import no.nav.meldinger.virksomhet.dokdistfordeling.qdist008.out.DistribuerTilKanal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static no.nav.dokdistdpv.qdist016.TestUtils.FORSENDELSE_ID;
import static no.nav.dokdistdpv.qdist016.TestUtils.createAltinnDokumenter;
import static no.nav.dokdistdpv.qdist016.TestUtils.createDistribuerTilKanal;
import static no.nav.dokdistdpv.qdist016.TestUtils.createHentForsendelseResponse;
import static no.nav.dokdistdpv.qdist016.TestUtils.createHentForsendelseResponseWithKonversasjonId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {Qdist016Service.class})
class Qdist016ServiceTest {

	private final HentForsendelseResponse HENT_FORSENDELSE_RESPONSE = createHentForsendelseResponse();
	private final List<AltinnDokument> ALTINN_DOKUMENTER = createAltinnDokumenter();
	private final DistribuerTilKanal DISTRIBUER_TIL_KANAL = createDistribuerTilKanal();

	@Autowired
	private Qdist016Service service;

	@MockBean
	private AdministrerForsendelseConsumer administrerForsendelseConsumer;

	@MockBean
	private DokumentService dokumentService;

	@MockBean
	private AltinnClient altinnClient;

	@Test
	void distribuerForsendelseTilDPV() {
		final ReceiptExternal receiptExternalOk = new ReceiptExternal();
		receiptExternalOk.setReceiptStatusCode(ReceiptStatusEnum.OK);

		when(administrerForsendelseConsumer.hentForsendelse(any())).thenReturn(HENT_FORSENDELSE_RESPONSE);
		when(dokumentService.hentDokumenter(any())).thenReturn(ALTINN_DOKUMENTER);
		doNothing().when(administrerForsendelseConsumer).oppdaterForsendelse(any(OppdaterForsendelseRequest.class));
		when(altinnClient.insertCorrespondence(
				HENT_FORSENDELSE_RESPONSE.konversasjonId(),
				HENT_FORSENDELSE_RESPONSE,
				ALTINN_DOKUMENTER)).thenReturn(receiptExternalOk);

		var result = service.distribuerForsendelseTilDPV(DISTRIBUER_TIL_KANAL);
		assertEquals(result, DISTRIBUER_TIL_KANAL.getForsendelseId());
	}

	@Test
	void distribuerForsendelseTilDPVHandterFeil() {
		final ReceiptExternal receiptExternalNotOk = new ReceiptExternal();
		receiptExternalNotOk.setReceiptStatusCode(ReceiptStatusEnum.VALIDATION_FAILED);

		when(administrerForsendelseConsumer.hentForsendelse(any())).thenReturn(HENT_FORSENDELSE_RESPONSE);
		when(dokumentService.hentDokumenter(any())).thenReturn(ALTINN_DOKUMENTER);
		doNothing().when(administrerForsendelseConsumer).oppdaterForsendelse(any(OppdaterForsendelseRequest.class));
		when(altinnClient.insertCorrespondence(
				HENT_FORSENDELSE_RESPONSE.konversasjonId(),
				HENT_FORSENDELSE_RESPONSE,
				ALTINN_DOKUMENTER)).thenReturn(receiptExternalNotOk);

		assertThrows(AltinnException.class, () ->
				service.distribuerForsendelseTilDPV(DISTRIBUER_TIL_KANAL));
	}

	@ParameterizedTest
	@ValueSource(strings = {TestUtils.KONVERSASJON_ID})
	@NullAndEmptySource
	void genererKonversasjonId(String konversasjonId) {

		var hentForsendelseResponse = createHentForsendelseResponseWithKonversasjonId(konversasjonId);
		var result = service.genererKonversasjonId(FORSENDELSE_ID, hentForsendelseResponse);

		assertNotNull(result);
	}
}