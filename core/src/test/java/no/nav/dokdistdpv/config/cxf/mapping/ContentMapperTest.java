package no.nav.dokdistdpv.config.cxf.mapping;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static no.nav.dokdistdpv.config.cxf.mapping.ContentMapper.mapFilename;
import static org.assertj.core.api.Assertions.assertThat;

class ContentMapperTest {

	@ParameterizedTest
	@MethodSource("provideFilenameTitles")
	void shouldMapAndCleanFilenames(String inputDokumenttittel, String expectedFilename) {
		String actualFilename = mapFilename("1000", inputDokumenttittel);

		assertThat(actualFilename).isEqualTo(expectedFilename);
	}

	private static Stream<Arguments> provideFilenameTitles() {
		return Stream.of(
				Arguments.of("Søknad om foreldrepenger", "1000Søknad om foreldrepenger.pdf"),
				Arguments.of("Samtale med Nav - 26.01.2024", "1000Samtale med Nav - 26.01.2024.pdf"),
				Arguments.of("Innvilget <15 pst. erstatning", "1000Innvilget 15 pst. erstatning.pdf"),
				Arguments.of("XX                 Overs.brev", "1000XX                 Overs.brev.pdf"),
				Arguments.of("Søknad om hjelpemiddel / ortopedisk middel", "1000Søknad om hjelpemiddel  ortopedisk middel.pdf"),
				Arguments.of("Ettersending av\\\\/:*?\"<>| dokumentasjon", "1000Ettersending av dokumentasjon.pdf"),
				Arguments.of("Helseskjema d.t\t.05.05.25\t", "1000Helseskjema d.t.05.05.25.pdf"),
				Arguments.of("Dokument (Uten relevant innhold)   ", "1000Dokument (Uten relevant innhold).pdf"),
				Arguments.of("Uønsket_hendelse_avvik_HMS_Utsatt_for_mye_marsipankake_ABCDEF5_000.pdf", "1000Uønsket_hendelse_avvik_HMS_Utsatt_for_mye_marsipankake_ABCDEF5_000.pdf")
		);
	}
}