package no.nav.dokdistdpv.consumer.altinn.map;

import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.left;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.apache.commons.lang3.StringUtils.removeEndIgnoreCase;
import static org.apache.commons.lang3.StringUtils.trim;

/// Helper for å mappe fileName og displayName
/// Vasker navn for ugyldige tegn
public class AttachmentNameMapper {
	private static final String BLANK_ERSTATNING = "";
	private static final String PUNKTUM = ".";
	private static final int FILE_NAME_MAX_CHARS = 255;
	private static final int DISPLAY_NAME_MAX_CHARS = 255;
	// https://learn.microsoft.com/en-us/windows/win32/fileio/naming-a-file#file-and-directory-names
	private static final Pattern FILE_NAME_UGYLDIGE_TEGN = Pattern.compile("[<>:\"/\\\\|?*\\u0000-\\u001f]");
	private static final Pattern DISPLAY_NAME_UGYLDIGE_TEGN = Pattern.compile("\\u0000");
	private static final String PDF_FILENDELSE = ".pdf";
	private static final int FILE_NAME_DIFF = FILE_NAME_MAX_CHARS - PDF_FILENDELSE.length();

	public static String mapFileName(long arkivDokumentInfoId, String tittel) {
		return mapFileName(arkivDokumentInfoId + "_" + tittel);
	}

	public static String mapFileName(String tittel) {
		return left(fjernPdfFilendelse(fjernUgyldigeFileNameTegn(trim(removeEnd(tittel, PUNKTUM)))), FILE_NAME_DIFF) + PDF_FILENDELSE;
	}

	public static String mapDisplayName(String tittel) {
		return left(fjernUgyldigeDisplayNameTegn(trim(tittel)), DISPLAY_NAME_MAX_CHARS);
	}

	private static String fjernPdfFilendelse(String vasketTekst) {
		return removeEndIgnoreCase(vasketTekst, PDF_FILENDELSE);
	}

	private static String fjernUgyldigeFileNameTegn(String tekst) {
		return FILE_NAME_UGYLDIGE_TEGN.matcher(tekst).replaceAll(BLANK_ERSTATNING);
	}

	private static String fjernUgyldigeDisplayNameTegn(String tekst) {
		return DISPLAY_NAME_UGYLDIGE_TEGN.matcher(tekst).replaceAll(BLANK_ERSTATNING);
	}
}
