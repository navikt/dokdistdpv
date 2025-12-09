package no.nav.dokdistdpv.qdist016.altinn3;

import java.util.Objects;

public record UploadedAttachment(String attachmentId, int rekkefoelge) implements Comparable<UploadedAttachment> {

	@Override
	public int compareTo(UploadedAttachment o) {
		return (rekkefoelge < o.rekkefoelge()) ? -1 : ((rekkefoelge == o.rekkefoelge) ? 0 : 1);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		UploadedAttachment that = (UploadedAttachment) o;
		return Objects.equals(attachmentId, that.attachmentId);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(attachmentId);
	}
}
