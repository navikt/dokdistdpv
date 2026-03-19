package no.nav.dokdistdpv.consumer.altinn.map;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.left;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.apache.commons.lang3.StringUtils.removeEndIgnoreCase;
import static org.apache.commons.lang3.StringUtils.trim;

/// Mapper for filnavn og visningsnavn. Vasker ugyldige tegn og begrenser lengden
///
/// * [Windows](https://learn.microsoft.com/en-us/windows/win32/fileio/naming-a-file#file-and-directory-names)
public class AttachmentNameMapper {
	private static final String BLANK_ERSTATNING = "";
	private static final String PUNKTUM = ".";
	/// Altinn API: 255 chars
	/// Windows: 255 chars
	/// Apple File System: 255 chars
	/// ext4: 255 bytes
	private static final int FILE_NAME_MAX_CHARS = 255;
	private static final int DISPLAY_NAME_MAX_CHARS = 255;
	private static final Pattern FILE_NAME_UGYLDIGE_TEGN_WINDOWS = Pattern.compile("[<>:\"/\\\\|?*\\u0000-\\u001f]");
	private static final Pattern DISPLAY_NAME_UGYLDIGE_TEGN = Pattern.compile("\\u0000");
	private static final String PDF_FILENDELSE = ".pdf";
	private static final int FILE_NAME_DIFF = FILE_NAME_MAX_CHARS - PDF_FILENDELSE.length();

	public static String mapFileName(long arkivDokumentInfoId, String tittel) {
		return mapFileName(arkivDokumentInfoId + "_" + tittel);
	}

	public static String mapFileName(String tittel) {
		return left(fjernUgyldigeFileNameTegn(removeEnd(trim(fjernPdfFilendelse(trim(tittel))), PUNKTUM)), FILE_NAME_DIFF) + PDF_FILENDELSE;
	}

	public static String mapZipEntryFileName(long arkivDokumentInfoId, String tittel) {
		return mapZipEntryFileName(arkivDokumentInfoId + "_" + tittel);
	}

	public static String mapZipEntryFileName(String tittel) {
		return leftBytes(fjernUgyldigeFileNameTegn(removeEnd(trim(fjernPdfFilendelse(trim(tittel))), PUNKTUM))) + PDF_FILENDELSE;
	}

	public static String mapDisplayName(String tittel) {
		return left(fjernUgyldigeDisplayNameTegn(trim(tittel)), DISPLAY_NAME_MAX_CHARS);
	}

	private static String fjernPdfFilendelse(String vasketTekst) {
		return removeEndIgnoreCase(vasketTekst, PDF_FILENDELSE);
	}

	private static String fjernUgyldigeFileNameTegn(String tekst) {
		return FILE_NAME_UGYLDIGE_TEGN_WINDOWS.matcher(tekst).replaceAll(BLANK_ERSTATNING);
	}

	private static String fjernUgyldigeDisplayNameTegn(String tekst) {
		return DISPLAY_NAME_UGYLDIGE_TEGN.matcher(tekst).replaceAll(BLANK_ERSTATNING);
	}

	/// Util for å beholde de første `tekst` bytes opp til `FILE_NAME_DIFF`
	/// Hindrer at unicode chars blir delt opp
	private static String leftBytes(String tekst) {
		if (tekst == null) return null;

		if (tekst.length() <= FILE_NAME_DIFF) {
			return tekst;
		}

		CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
		CharBuffer charBuffer = CharBuffer.wrap(tekst);
		ByteBuffer byteBuffer = ByteBuffer.allocate(FILE_NAME_DIFF);
		encoder.encode(charBuffer, byteBuffer, true);
		byteBuffer.flip();
		return new String(byteBuffer.array(), 0, byteBuffer.limit(), StandardCharsets.UTF_8);
	}
}
