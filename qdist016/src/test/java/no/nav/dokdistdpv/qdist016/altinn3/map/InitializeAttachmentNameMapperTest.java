package no.nav.dokdistdpv.qdist016.altinn3.map;

import no.nav.dokdistdpv.qdist016.dokument.NavDokument;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.stream.Stream;

import static no.nav.dokdistdpv.consumer.altinn.map.AttachmentNameMapperTest.ARKIVERTE_TITLER_FILENAME_ASSERT;
import static no.nav.dokdistdpv.consumer.altinn.map.AttachmentNameMapperTest.ARKIVERTE_TITLER_ZIP_FILENAME_ASSERT;
import static no.nav.dokdistdpv.consumer.altinn.map.AttachmentNameMapperTest.IKKE_ARKIVERTE_TITLER_FILENAME_ASSERT;
import static no.nav.dokdistdpv.consumer.altinn.map.AttachmentNameMapperTest.IKKE_ARKIVERTE_TITLER_ZIP_FILENAME_ASSERT;
import static no.nav.dokdistdpv.qdist016.altinn3.map.InitializeAttachmentNameMapper.mapFileName;
import static no.nav.dokdistdpv.qdist016.altinn3.map.InitializeAttachmentNameMapper.mapZipEntryFileName;
import static org.assertj.core.api.Assertions.assertThat;

class InitializeAttachmentNameMapperTest {

	@ParameterizedTest
	@MethodSource("provideFilenameTitles")
	void shouldMapAndCleanFilenames(String tittel, String expectedFilename) {
		String actualFilename = mapFileName(new NavDokument(1000L, UUID.randomUUID().toString(), tittel, 1, 0));

		assertThat(actualFilename).isEqualTo(expectedFilename);
	}

	private static Stream<Arguments> provideFilenameTitles() {
		return Stream.of(ARKIVERTE_TITLER_FILENAME_ASSERT);
	}

	@ParameterizedTest
	@MethodSource("provideFilenameTitlesIkkeArkivertIJoark")
	void shouldMapAndCleanFilenamesIkkeArkivertIJoark(String tittel, String expectedFilename) {
		String actualFilename = mapFileName(new NavDokument(null, UUID.randomUUID().toString(), tittel, 1, 0));

		assertThat(actualFilename).isEqualTo(expectedFilename);
	}

	private static Stream<Arguments> provideFilenameTitlesIkkeArkivertIJoark() {
		return Stream.of(IKKE_ARKIVERTE_TITLER_FILENAME_ASSERT);
	}

	@ParameterizedTest
	@MethodSource("provideZipEntryFilenameTitles")
	void shouldMapAndCleanZipEntryFilenames(String tittel, String expectedFilename) {
		String actualFilename = mapZipEntryFileName(new NavDokument(1000L, UUID.randomUUID().toString(), tittel, 1, 0));

		assertThat(actualFilename).isEqualTo(expectedFilename);
	}

	private static Stream<Arguments> provideZipEntryFilenameTitles() {
		return Stream.of(ARKIVERTE_TITLER_ZIP_FILENAME_ASSERT);
	}

	@ParameterizedTest
	@MethodSource("provideZipEntryFilenameTitlesIkkeArkivertIJoark")
	void shouldMapAndCleanZipEntryFilenamesIkkeArkivertIJoark(String tittel, String expectedFilename) {
		String actualFilename = mapZipEntryFileName(new NavDokument(null, UUID.randomUUID().toString(), tittel, 1, 0));

		assertThat(actualFilename).isEqualTo(expectedFilename);
	}

	private static Stream<Arguments> provideZipEntryFilenameTitlesIkkeArkivertIJoark() {
		return Stream.of(IKKE_ARKIVERTE_TITLER_ZIP_FILENAME_ASSERT);
	}
}