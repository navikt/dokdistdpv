package no.nav.dokdistdpv.consumer.altinn2.mapping;

import no.altinn.correspondenceagencyexternalaec.AttachmentsV2;
import no.altinn.correspondenceagencyexternalaec.BinaryAttachmentExternalBEV2List;
import no.altinn.correspondenceagencyexternalaec.BinaryAttachmentV2;
import no.altinn.correspondenceagencyexternalaec.ExternalContentV2;
import no.nav.dokdistdpv.consumer.rdist001.domain.DistribusjonsTypeKode;
import no.nav.dokdistdpv.consumer.rdist001.domain.HentForsendelseResponse;

import java.util.List;

import static no.altinn.correspondenceagencyexternalaec.AttachmentFunctionType.UNSPECIFIED;
import static no.altinn.correspondenceagencyexternalaec.UserTypeRestriction.DEFAULT;
import static no.nav.dokdistdpv.utils.AltinnCorrespondenceConstant.LANGUAGE_CODE_BOKMAAL;
import static no.nav.dokdistdpv.utils.AltinnCorrespondenceConstant.MESSAGE_TITLE_ANNET;
import static no.nav.dokdistdpv.utils.AltinnCorrespondenceConstant.MESSAGE_TITLE_VEDTAK;
import static no.nav.dokdistdpv.utils.AltinnCorrespondenceConstant.MESSAGE_TITLE_VIKTIG;

public class ContentMapper {

	public static ExternalContentV2 mapContent(
			HentForsendelseResponse forsendelse,
			List<AltinnDokument> dokumenter
	) {
		ExternalContentV2 content = new ExternalContentV2();

		content.setLanguageCode(LANGUAGE_CODE_BOKMAAL);
		content.setMessageTitle(mapMessageTitle(forsendelse.distribusjonstype()));
		content.setMessageBody(mapMessageBody(forsendelse.distribusjonstype(), forsendelse.forsendelseTittel()));
		content.setAttachments(mapAttachments(forsendelse, dokumenter));

		return content;
	}

	private static AttachmentsV2 mapAttachments(
			HentForsendelseResponse forsendelse,
			List<AltinnDokument> dokumenter
	) {
		AttachmentsV2 attachments = new AttachmentsV2();

		BinaryAttachmentExternalBEV2List binaryAttachments = new BinaryAttachmentExternalBEV2List();
		for (int i = 0; i < forsendelse.dokumenter().size(); i++) {
			var forsendelsedokument = forsendelse.dokumenter().get(i);
			var dokument = dokumenter.get(i);

			BinaryAttachmentV2 attachment = new BinaryAttachmentV2();
			attachment.setDestinationType(DEFAULT);
			attachment.setFunctionType(UNSPECIFIED);
			attachment.setFileName(mapFilename(forsendelsedokument.arkivDokumentInfoId(), dokument.tittel()));
			attachment.setName(dokument.tittel());
			attachment.setEncrypted(false);
			attachment.setData(dokument.pdf());
			attachment.setSendersReference(forsendelsedokument.dokumentObjektReferanse());

			binaryAttachments.getBinaryAttachmentV2().add(attachment);
		}
		attachments.setBinaryAttachments(binaryAttachments);

		return attachments;
	}

	static String mapFilename(String arkivDokumentInfoId, String dokumenttittel) {
		String cleanedTittel = dokumenttittel.replaceAll("[\\\\/:*?\"<>|\\t]|\\s+$", "");
		if(cleanedTittel.endsWith(".pdf")) {
			return arkivDokumentInfoId + cleanedTittel;
		}
		return arkivDokumentInfoId + cleanedTittel + ".pdf";
	}

	public static String mapMessageTitle(DistribusjonsTypeKode distribusjonstype) {
		if (distribusjonstype == null) {
			return MESSAGE_TITLE_VIKTIG;
		}

		return switch (distribusjonstype) {
			case VEDTAK -> MESSAGE_TITLE_VEDTAK;
			case VIKTIG -> MESSAGE_TITLE_VIKTIG;
			case ANNET -> MESSAGE_TITLE_ANNET;
		};
	}

	public static String mapMessageBody(DistribusjonsTypeKode distribusjonstype, String forsendelseTittel) {
		if (distribusjonstype == null) {
			return String.format("Du har fått et brev som du må lese: %s.", forsendelseTittel);
		}

		return switch (distribusjonstype) {
			case VEDTAK -> String.format("Du har fått et vedtak som gjelder %s.", forsendelseTittel);
			case ANNET -> String.format("Du har fått en melding som gjelder %s.", forsendelseTittel);
			case VIKTIG -> String.format("Du har fått et brev som du må lese: %s.", forsendelseTittel);
		};
	}
}
