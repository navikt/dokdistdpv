package no.nav.dokdistdpv.consumer.rdist001.domain;

public record OppdaterForsendelseRequest(Long forsendelseId,
										 String forsendelseStatus,
										 String konversasjonId) {
	private static final String FORSENDELSE_STATUS_OVERSENDT = "OVERSENDT";
	private static final String FORSENDELSE_STATUS_EKSPEDERT = "EKSPEDERT";

	public static OppdaterForsendelseRequest oversendt(long forsendelseId, String konversasjonId) {
		return new OppdaterForsendelseRequest(forsendelseId, FORSENDELSE_STATUS_OVERSENDT, konversasjonId);
	}

	public static OppdaterForsendelseRequest ekspedert(long forsendelseId) {
		return new OppdaterForsendelseRequest(forsendelseId, FORSENDELSE_STATUS_EKSPEDERT, null);
	}

	public static OppdaterForsendelseRequest konversasjonId(long forsendelseId, String konversasjonId) {
		return new OppdaterForsendelseRequest(forsendelseId, null, konversasjonId);
	}
}
