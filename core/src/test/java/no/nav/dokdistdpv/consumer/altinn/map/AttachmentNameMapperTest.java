package no.nav.dokdistdpv.consumer.altinn.map;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static no.nav.dokdistdpv.consumer.altinn.map.AttachmentNameMapper.mapDisplayName;
import static no.nav.dokdistdpv.consumer.altinn.map.AttachmentNameMapper.mapFileName;
import static org.assertj.core.api.Assertions.assertThat;

public class AttachmentNameMapperTest {

	@ParameterizedTest
	@MethodSource("provideFilenameTitles")
	void shouldMapAndCleanFilenames(String tittel, String expectedFilename) {
		String actualFilename = mapFileName(1000L, tittel);

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
				Arguments.of("Søknad om foreldrepenger.pdf ", "1000_Søknad om foreldrepenger.pdf"),
				Arguments.of("Søknad om foreldrepenger. ", "1000_Søknad om foreldrepenger.pdf"),
				Arguments.of("Søknad om foreldrepenger. .pdf", "1000_Søknad om foreldrepenger.pdf"),
				Arguments.of("Søknad om foreldrepenger  .pdf", "1000_Søknad om foreldrepenger.pdf"),
				Arguments.of("ABCDEF-12345.jpeg", "1000_ABCDEF-12345.jpeg.pdf")
		);
	}

	@ParameterizedTest
	@MethodSource("provideFilenameTitlesIkkeArkivertIJoark")
	void shouldMapAndCleanFilenamesIkkeArkivertIJoark(String tittel, String expectedFilename) {
		String actualFilename = mapFileName(tittel);

		assertThat(actualFilename).isEqualTo(expectedFilename);
	}

	private static Stream<Arguments> provideFilenameTitlesIkkeArkivertIJoark() {
		return Stream.of(
				Arguments.of("Søknad om foreldrepenger", "Søknad om foreldrepenger.pdf"),
				Arguments.of("Søknad om foreldrepenger.pdf", "Søknad om foreldrepenger.pdf"),
				Arguments.of("Søknad om foreldrepenger.PDF", "Søknad om foreldrepenger.pdf"),
				Arguments.of("Samtale med Nav - 26.01.2024", "Samtale med Nav - 26.01.2024.pdf"),
				Arguments.of("Innvilget <15 pst. erstatning", "Innvilget 15 pst. erstatning.pdf"),
				Arguments.of("XX                 Overs.brev", "XX                 Overs.brev.pdf"),
				Arguments.of("Søknad om hjelpemiddel / ortopedisk middel", "Søknad om hjelpemiddel  ortopedisk middel.pdf"),
				Arguments.of("Ettersending av\\\\/:*?\"<>| dokumentasjon", "Ettersending av dokumentasjon.pdf"),
				Arguments.of("Helseskjema d.t\t.05.05.25\t", "Helseskjema d.t.05.05.25.pdf"),
				Arguments.of("Dokument (Uten relevant innhold)   ", "Dokument (Uten relevant innhold).pdf"),
				Arguments.of("Uønsket_hendelse_avvik_HMS_Utsatt_for_mye_marsipankake_ABCDEF5_000.pdf", "Uønsket_hendelse_avvik_HMS_Utsatt_for_mye_marsipankake_ABCDEF5_000.pdf"),
				Arguments.of("a".repeat(256), "a".repeat(251) + ".pdf"),
				Arguments.of("\0Søknad om hjelpemiddel / \0ortopedisk middel", "Søknad om hjelpemiddel  ortopedisk middel.pdf"),
				Arguments.of("Søknad om pdf.pdf", "Søknad om pdf.pdf"),
				Arguments.of("Søknad \u0002om foreldrep\u001Fenger", "Søknad om foreldrepenger.pdf"),
				Arguments.of("Søknad om foreldrepenger.", "Søknad om foreldrepenger.pdf"),
				Arguments.of("Søknad om foreldrepenger.pdf ", "Søknad om foreldrepenger.pdf"),
				Arguments.of("Søknad om foreldrepenger. ", "Søknad om foreldrepenger.pdf"),
				Arguments.of("Søknad om foreldrepenger. .pdf", "Søknad om foreldrepenger.pdf"),
				Arguments.of("Søknad om foreldrepenger  .pdf", "Søknad om foreldrepenger.pdf"),
				Arguments.of("ABCDEF-12345.jpeg", "ABCDEF-12345.jpeg.pdf")
		);
	}

	@ParameterizedTest
	@MethodSource("provideDisplayNameTitles")
	void shouldMapAndCleanDisplayNames(String tittel, String expectedTittel) {
		String actualDisplayName = mapDisplayName(tittel);

		assertThat(actualDisplayName).isEqualTo(expectedTittel);
	}

	private static Stream<Arguments> provideDisplayNameTitles() {
		return Stream.of(
				Arguments.of("Søknad om foreldrepenger", "Søknad om foreldrepenger"),
				Arguments.of("Samtale med Nav - 26.01.2024", "Samtale med Nav - 26.01.2024"),
				Arguments.of("Innvilget <15 pst. erstatning", "Innvilget <15 pst. erstatning"),
				Arguments.of("XX                 Overs.brev", "XX                 Overs.brev"),
				Arguments.of("Søknad om hjelpemiddel / ortopedisk middel", "Søknad om hjelpemiddel / ortopedisk middel"),
				Arguments.of("Ettersending av\\\\/:*?\"<>| dokumentasjon", "Ettersending av\\\\/:*?\"<>| dokumentasjon"),
				Arguments.of("Helseskjema d.t\t.05.05.25\t", "Helseskjema d.t\t.05.05.25"),
				Arguments.of("Dokument (Uten relevant innhold)   ", "Dokument (Uten relevant innhold)"),
				Arguments.of("Uønsket_hendelse_avvik_HMS_Utsatt_for_mye_marsipankake_ABCDEF5_000.pdf", "Uønsket_hendelse_avvik_HMS_Utsatt_for_mye_marsipankake_ABCDEF5_000.pdf"),
				Arguments.of("a".repeat(256), "a".repeat(255)),
				Arguments.of("\0Søknad om hjelpemiddel / \0ortopedisk middel", "Søknad om hjelpemiddel / ortopedisk middel"),
				Arguments.of("Søknad om pdf.pdf", "Søknad om pdf.pdf"),
				Arguments.of("Søknad \u0002om foreldrep\u001Fenger", "Søknad \u0002om foreldrep\u001Fenger"),
				Arguments.of("Søknad om foreldrepenger.", "Søknad om foreldrepenger."),
				Arguments.of("Søknad om foreldrepenger.pdf ", "Søknad om foreldrepenger.pdf"),
				Arguments.of("Søknad om foreldrepenger. ", "Søknad om foreldrepenger."),
				Arguments.of("Søknad om foreldrepenger. .pdf", "Søknad om foreldrepenger. .pdf"),
				Arguments.of("Søknad om foreldrepenger  .pdf", "Søknad om foreldrepenger  .pdf"),
				Arguments.of("ABCDEF-12345.jpeg", "ABCDEF-12345.jpeg")
		);
	}
}