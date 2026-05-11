package no.nav.dokdistdpv.consumer.altinn3.api.correspondence;

///  [Correspondence error codes](https://docs.altinn.studio/nb/api/correspondence/spec/#/Correspondence/post_correspondence_api_v1_correspondence)
public final class CorrespondenceErrorCodes {
	public static final int ATTACHMENT_IS_NOT_PUBLISHED = 1018;
	public static final int RECIPIENTS_LACK_REQUIRED_ROLES = 1044;

	private CorrespondenceErrorCodes() {
		// noop
	}
}
