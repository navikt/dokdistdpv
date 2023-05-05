package no.nav.dokdistdpv.consumer.rdist001.domain;

public record OppdaterForsendelseRequest(Long forsendelseId,
										 String forsendelseStatus,
										 String konversasjonId) {
}
