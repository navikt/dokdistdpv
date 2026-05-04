package no.nav.dokdistdpv.kdist003.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

@Configuration
public class KafkaErrorHandlerConfig {

	@Bean
	@ConditionalOnMissingBean(CommonErrorHandler.class)
	public CommonErrorHandler kafkaErrorHandler() {

		// maxRetries=10, initialInterval=2000ms, multiplier=1.5s gir totalt ~2min 34s fra første forsøk til siste forsøk
		var backOff = new ExponentialBackOffWithMaxRetries(10);
		return new DefaultErrorHandler(backOff);
	}
}
