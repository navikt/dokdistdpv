package no.nav.dokdistdpv.kdist003.config;

import no.altinn.event.domain.CloudEvent;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class KafkaConsumerConfig {

	@Bean
	public ConsumerFactory<String, CloudEvent> consumerFactory(KafkaProperties kafkaProperties, JsonMapper jsonMapper) {
		var factory = new DefaultKafkaConsumerFactory<String, CloudEvent>(kafkaProperties.buildConsumerProperties());
		factory.setKeyDeserializer(new StringDeserializer());
		var deserializer = new JacksonJsonDeserializer<>(CloudEvent.class, jsonMapper);
		factory.setValueDeserializer(deserializer);
		return factory;
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, CloudEvent> kafkaListenerContainerFactory(
			ConsumerFactory<String, CloudEvent> consumerFactory,
			ObjectProvider<CommonErrorHandler> errorHandler) {
		ConcurrentKafkaListenerContainerFactory<String, CloudEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory);
		errorHandler.ifUnique(factory::setCommonErrorHandler);
		return factory;
	}
}
