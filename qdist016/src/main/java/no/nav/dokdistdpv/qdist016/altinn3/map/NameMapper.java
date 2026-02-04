package no.nav.dokdistdpv.qdist016.altinn3.map;

import no.nav.dokdistdpv.qdist016.dokument.NavDokument;

import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.left;
import static org.apache.commons.lang3.StringUtils.stripEnd;

public final class NameMapper {
	private static final String BLANK_ERSTATNING = "";
	private static final String PDF_FILENDELSE = ".pdf";
	private static final int FILE_NAME_MAX_CHARS = 256;
	private static final int FILE_NAME_DIFF = FILE_NAME_MAX_CHARS - PDF_FILENDELSE.length();
	private static final int DISPLAY_NAME_MAX_CHARS = 256;
	private static final Pattern UGYLDIGE_TEGN = Pattern.compile("[\u0000\\\\/:*?\"<>|\\t]|\\s+$");

	public static String mapFileName(NavDokument navDokument) {
		if (navDokument.isArkivertIJoark()) {
			return left(fjernPdfFilendelse(fjernUgyldigeTegn(navDokument.arkivDokumentInfoId() + "_" + navDokument.tittel())), FILE_NAME_DIFF) + ".pdf";
		}
		return left(fjernPdfFilendelse(fjernUgyldigeTegn(navDokument.tittel())), FILE_NAME_DIFF) + PDF_FILENDELSE;
	}

	public static String mapDisplayName(String tittel) {
		return left(tittel, DISPLAY_NAME_MAX_CHARS);
	}

	private static String fjernPdfFilendelse(String vasketTekst) {
		if (vasketTekst.endsWith(PDF_FILENDELSE)) {
			return stripEnd(vasketTekst, PDF_FILENDELSE);
		}
		return vasketTekst;
	}

	private static String fjernUgyldigeTegn(String tekst) {
		return UGYLDIGE_TEGN.matcher(tekst).replaceAll(BLANK_ERSTATNING);
	}
}
