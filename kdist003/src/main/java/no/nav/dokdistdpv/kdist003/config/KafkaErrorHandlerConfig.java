package no.nav.dokdistdpv.kdist003.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.serializer.DeserializationException;

@Configuration
public class KafkaErrorHandlerConfig {

	@Bean
	@ConditionalOnMissingBean(CommonErrorHandler.class)
	public CommonErrorHandler kafkaErrorHandler() {

		// maxRetries=10, initialInterval=2000ms, multiplier=1.5 gir totalt ~2min 34s fra første forsøk til siste forsøk
		var backOff = new ExponentialBackOffWithMaxRetries(10);
		backOff.setInitialInterval(2000L);
		backOff.setMultiplier(1.5);

		var errorHandler = new DefaultErrorHandler(backOff);
		errorHandler.addNotRetryableExceptions(DeserializationException.class);
		return errorHandler;
	}
}
