package no.nav.dokdistdpv.config.cxf.mapping;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static no.nav.dokdistdpv.config.cxf.mapping.ContentMapper.mapFileName;
import static org.assertj.core.api.Assertions.assertThat;

class ContentMapperTest {

	@ParameterizedTest
	@MethodSource("provideFilenameTitles")
	void shouldMapFilenameAndRemoveIllegalFilenameChars(String inputDokumenttittel, String expectedDokumenttittel) {
		String actualDokumenttittel = mapFileName("1000", inputDokumenttittel);

		assertThat(actualDokumenttittel).isEqualTo(expectedDokumenttittel);
	}

	private static Stream<Arguments> provideFilenameTitles() {
		return Stream.of(
				Arguments.of("Søknad om foreldrepenger", "1000Søknad om foreldrepenger.pdf"),
				Arguments.of("Samtale med Nav - 26.01.2024", "1000Samtale med Nav - 26.01.2024.pdf"),
				Arguments.of("Innvilget <15 pst. erstatning", "1000Innvilget 15 pst. erstatning.pdf"),
				Arguments.of("XX                 Overs.brev", "1000XX                 Overs.brev.pdf"),
				Arguments.of("Søknad om hjelpemiddel / ortopedisk middel", "1000Søknad om hjelpemiddel  ortopedisk middel.pdf"),
				Arguments.of("Ettersending av\\\\/:*?\"<>| dokumentasjon", "1000Ettersending av dokumentasjon.pdf")
		);
	}
}