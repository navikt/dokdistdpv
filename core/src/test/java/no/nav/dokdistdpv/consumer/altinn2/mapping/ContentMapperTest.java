package no.nav.dokdistdpv.consumer.altinn2.mapping;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static no.nav.dokdistdpv.consumer.altinn2.mapping.ContentMapper.mapFileName;
import static org.assertj.core.api.Assertions.assertThat;

class ContentMapperTest {

	@ParameterizedTest
	@MethodSource("provideFilenameTitles")
	void shouldMapAndCleanFilenames(String inputDokumenttittel, String expectedFilename) {
		String actualFilename = mapFileName("1000", inputDokumenttittel);

		assertThat(actualFilename).isEqualTo(expectedFilename);
	}

	private static Stream<Arguments> provideFilenameTitles() {
		return Stream.of(
				Arguments.of("Søknad om foreldrepenger", "1000_Søknad om foreldrepenger.pdf"),
				Arguments.of("Søknad om foreldrepenger.pdf", "1000_Søknad om foreldrepenger.pdf"),
				Arguments.of("Søknad om foreldrepenger.PDF", "1000_Søknad om foreldrepenger.pdf"),
				Arguments.of("Samtale med Nav - 26.01.2024", "1000_Samtale med Nav - 26.01.2024.pdf"),
				Arguments.of("Innvilget <15 pst. erstatning", "1000_Innvilget 15 pst. erstatning.pdf"),
				Arguments.of("XX                 Overs.brev", "1000_XX                 Overs.brev.pdf"),
				Arguments.of("Søknad om hjelpemiddel / ortopedisk middel", "1000_Søknad om hjelpemiddel  ortopedisk middel.pdf"),
				Arguments.of("Ettersending av\\\\/:*?\"<>| dokumentasjon", "1000_Ettersending av dokumentasjon.pdf"),
				Arguments.of("Helseskjema d.t\t.05.05.25\t", "1000_Helseskjema d.t.05.05.25.pdf"),
				Arguments.of("Dokument (Uten relevant innhold)   ", "1000_Dokument (Uten relevant innhold).pdf"),
				Arguments.of("Uønsket_hendelse_avvik_HMS_Utsatt_for_mye_marsipankake_ABCDEF5_000.pdf", "1000_Uønsket_hendelse_avvik_HMS_Utsatt_for_mye_marsipankake_ABCDEF5_000.pdf"),
				Arguments.of("a".repeat(256), "1000_" + "a".repeat(246) + ".pdf"),
				Arguments.of("\0Søknad om hjelpemiddel / \0ortopedisk middel", "1000_Søknad om hjelpemiddel  ortopedisk middel.pdf"),
				Arguments.of("Søknad om pdf.pdf", "1000_Søknad om pdf.pdf"),
				Arguments.of("Søknad \u0002om foreldrep\u001Fenger", "1000_Søknad om foreldrepenger.pdf"),
				Arguments.of("Søknad om foreldrepenger.", "1000_Søknad om foreldrepenger.pdf"),
				Arguments.of("ABCDEF-12345.jpeg", "1000_ABCDEF-12345.jpeg.pdf")
		);
	}
}