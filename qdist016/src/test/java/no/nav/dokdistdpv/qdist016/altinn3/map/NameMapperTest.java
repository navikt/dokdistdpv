package no.nav.dokdistdpv.qdist016.altinn3.map;

import no.nav.dokdistdpv.qdist016.dokument.NavDokument;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static no.nav.dokdistdpv.qdist016.altinn3.map.NameMapper.mapFileName;
import static org.assertj.core.api.Assertions.assertThat;

class NameMapperTest {

	@ParameterizedTest
	@MethodSource("provideFilenameTitles")
	void shouldMapAndCleanFilenames(String tittel, String expectedFilename) {
		String actualFilename = mapFileName(new NavDokument(1000L, null, tittel, 1, 0));

		assertThat(actualFilename).isEqualTo(expectedFilename);
	}

	private static Stream<Arguments> provideFilenameTitles() {
		return Stream.of(
				Arguments.of("Søknad om foreldrepenger", "1000_Søknad om foreldrepenger.pdf"),
				Arguments.of("Samtale med Nav - 26.01.2024", "1000_Samtale med Nav - 26.01.2024.pdf"),
				Arguments.of("Innvilget <15 pst. erstatning", "1000_Innvilget 15 pst. erstatning.pdf"),
				Arguments.of("XX                 Overs.brev", "1000_XX                 Overs.brev.pdf"),
				Arguments.of("Søknad om hjelpemiddel / ortopedisk middel", "1000_Søknad om hjelpemiddel  ortopedisk middel.pdf"),
				Arguments.of("Ettersending av\\\\/:*?\"<>| dokumentasjon", "1000_Ettersending av dokumentasjon.pdf"),
				Arguments.of("Helseskjema d.t\t.05.05.25\t", "1000_Helseskjema d.t.05.05.25.pdf"),
				Arguments.of("Dokument (Uten relevant innhold)   ", "1000_Dokument (Uten relevant innhold).pdf"),
				Arguments.of("Uønsket_hendelse_avvik_HMS_Utsatt_for_mye_marsipankake_ABCDEF5_000.pdf", "1000_Uønsket_hendelse_avvik_HMS_Utsatt_for_mye_marsipankake_ABCDEF5_000.pdf"),
				Arguments.of("hepp".repeat(65), "1000_" + "hepp".repeat(61) + "hep.pdf"),
				Arguments.of("\0Søknad om hjelpemiddel / \0ortopedisk middel", "1000_Søknad om hjelpemiddel  ortopedisk middel.pdf")
		);
	}

	@ParameterizedTest
	@MethodSource("provideFilenameTitlesIkkeArkivertIJoark")
	void shouldMapAndCleanFilenamesIkkeArkivertIJoark(String tittel, String expectedFilename) {
		String actualFilename = mapFileName(new NavDokument(null, null, tittel, 1, 0));

		assertThat(actualFilename).isEqualTo(expectedFilename);
	}

	private static Stream<Arguments> provideFilenameTitlesIkkeArkivertIJoark() {
		return Stream.of(
				Arguments.of("Søknad om foreldrepenger", "Søknad om foreldrepenger.pdf"),
				Arguments.of("Samtale med Nav - 26.01.2024", "Samtale med Nav - 26.01.2024.pdf"),
				Arguments.of("Innvilget <15 pst. erstatning", "Innvilget 15 pst. erstatning.pdf"),
				Arguments.of("XX                 Overs.brev", "XX                 Overs.brev.pdf"),
				Arguments.of("Søknad om hjelpemiddel / ortopedisk middel", "Søknad om hjelpemiddel  ortopedisk middel.pdf"),
				Arguments.of("Ettersending av\\\\/:*?\"<>| dokumentasjon", "Ettersending av dokumentasjon.pdf"),
				Arguments.of("Helseskjema d.t\t.05.05.25\t", "Helseskjema d.t.05.05.25.pdf"),
				Arguments.of("Dokument (Uten relevant innhold)   ", "Dokument (Uten relevant innhold).pdf"),
				Arguments.of("Uønsket_hendelse_avvik_HMS_Utsatt_for_mye_marsipankake_ABCDEF5_000.pdf", "Uønsket_hendelse_avvik_HMS_Utsatt_for_mye_marsipankake_ABCDEF5_000.pdf"),
				Arguments.of("hepp".repeat(65), "hepp".repeat(63) + ".pdf"),
				Arguments.of("\0Søknad om hjelpemiddel / \0ortopedisk middel", "Søknad om hjelpemiddel  ortopedisk middel.pdf")
		);
	}
}