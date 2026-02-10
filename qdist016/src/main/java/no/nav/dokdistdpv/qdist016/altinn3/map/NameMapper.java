package no.nav.dokdistdpv.qdist016.altinn3.map;

import no.nav.dokdistdpv.qdist016.dokument.NavDokument;

import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.left;
import static org.apache.commons.lang3.StringUtils.removeEndIgnoreCase;

public final class NameMapper {
	private static final String BLANK_ERSTATNING = "";
	private static final String PDF_FILENDELSE = ".pdf";
	private static final int FILE_NAME_MAX_CHARS = 255;
	private static final int FILE_NAME_DIFF = FILE_NAME_MAX_CHARS - PDF_FILENDELSE.length();
	private static final int DISPLAY_NAME_MAX_CHARS = 255;
	private static final Pattern FILE_NAME_UGYLDIGE_TEGN = Pattern.compile("[\u0000\\\\/:*?\"<>|\\t]|\\s+$");
	private static final Pattern DISPLAY_NAME_UGYLDIGE_TEGN = Pattern.compile("\u0000|\\s+$");

	public static String mapFileName(NavDokument navDokument) {
		if (navDokument.isArkivertIJoark()) {
			Long arkivDokumentInfoId = navDokument.arkivDokumentInfoId();
			return left(fjernPdfFilendelse(fjernUgyldigeFileNameTegn(arkivDokumentInfoId + "_" + navDokument.tittel())), FILE_NAME_DIFF) + PDF_FILENDELSE;
		}
		return left(fjernPdfFilendelse(fjernUgyldigeFileNameTegn(navDokument.tittel())), FILE_NAME_DIFF) + PDF_FILENDELSE;
	}

	public static String mapDisplayName(String tittel) {
		return left(fjernUgyldigeDisplayNameTegn(tittel), DISPLAY_NAME_MAX_CHARS);
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
