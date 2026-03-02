package no.nav.dokdistdpv.kdist003.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.nav.dokdigdirhendelser.altinn.AltinnEvent;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.time.Duration.ofSeconds;
import static java.util.Collections.singletonList;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@DirtiesContext
@EnableAutoConfiguration
@SpringBootTest(
		classes = {ApplicationTestConfig.class},
		webEnvironment = NONE
)
@AutoConfigureWireMock(port = 0)
@EmbeddedKafka(partitions = 1, topics = {"altinn-melding-hendelse"},
		brokerProperties = {
				"offsets.topic.replication.factor=1",
		})
@ActiveProfiles("itest")
public abstract class AbstractIT {

	protected static final String PRIVAT_ALTINN_MELDING_TOPIC = "altinn-melding-hendelse";
	public static final String FINN_FORSENDELSE_PATH = "/administrerforsendelse/finnforsendelse/konversasjonsId/af0e7e0c-579c-4563-9398-10cdf031b80d";
	public static final String HENT_FORSENDELSE_PATH = "/administrerforsendelse";
	public static final String OPPDATERDISTRIBUSJONSINFO_URL = "/rest/journalpostapi/v1/journalpost/.*/oppdaterDistribusjonsinfo";
	public static final String DISTRIBUERTILPRINT_PATH = "/administrerforsendelse/distribuertilnykanal";


	@Autowired
	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	protected EmbeddedKafkaBroker embeddedKafkaBroker;
	private ObjectMapper objectMapper;
	protected Consumer<String, AltinnEvent> consumer;
	protected Producer<String, AltinnEvent> producer;

	@BeforeEach
	void setUp() {
		objectMapper = objectMapper();
		producer = createProducer();
		consumer = createConsumer();

		deleteMessages();
	}

	@AfterEach
	void tearDown() {
		if (consumer != null) {
			consumer.close();
		}
		if (producer != null) {
			producer.close();
		}
	}

	protected Producer<String, AltinnEvent> createProducer() {
		Map<String, Object> configs = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));

		JsonSerializer<AltinnEvent> valueSerializer = new JsonSerializer<>(objectMapper);
		valueSerializer.setAddTypeInfo(false);
		return new DefaultKafkaProducerFactory<>(configs, new StringSerializer(), valueSerializer).createProducer();
	}

	protected Consumer<String, AltinnEvent> createConsumer() {
		String uniqueGroupId = "itest-group-" + UUID.randomUUID();
		Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(uniqueGroupId, "true", embeddedKafkaBroker);
		consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

		JsonDeserializer<AltinnEvent> valueDeserializer = new JsonDeserializer<>(AltinnEvent.class, objectMapper);
		valueDeserializer.setUseTypeHeaders(false);
		valueDeserializer.addTrustedPackages("*");

		StringDeserializer keyDeserializer = new StringDeserializer();

		Consumer<String, AltinnEvent> newConsumer = new DefaultKafkaConsumerFactory<>(
				consumerProps,
				keyDeserializer,
				valueDeserializer
		).createConsumer();

		newConsumer.subscribe(singletonList(PRIVAT_ALTINN_MELDING_TOPIC));
		return newConsumer;
	}

	private void deleteMessages() {
		ConsumerRecords<String, AltinnEvent> records;
		do {
			records = consumer.poll(Duration.ofSeconds(10));
		} while (!records.isEmpty());
	}

	protected void sendToInnTopic(AltinnEvent altinnEvent) {
		producer.send(new ProducerRecord<>(PRIVAT_ALTINN_MELDING_TOPIC, altinnEvent));
		producer.flush();
	}

	public ConsumerRecord<String, AltinnEvent> getConsumerRecord() {
		return KafkaTestUtils.getSingleRecord(consumer, PRIVAT_ALTINN_MELDING_TOPIC, ofSeconds(15));
	}

	ObjectMapper objectMapper() {
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		return objectMapper;
	}
}
