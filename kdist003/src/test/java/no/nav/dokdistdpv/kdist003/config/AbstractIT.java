package no.nav.dokdistdpv.kdist003.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.nav.dokdistdpv.kdist003.domain.AltinnEventMelding;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
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

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@DirtiesContext
@EnableAutoConfiguration
@SpringBootTest(
		classes = {ApplicationTestConfig.class},
		webEnvironment = NONE
)
@AutoConfigureWireMock(port = 0)
@EmbeddedKafka(partitions = 1, topics = {"test-ut-topic"},
		brokerProperties = {
				"offsets.topic.replication.factor=1",
				"transaction.state.log.replication.factor=1",
				"transaction.state.log.min.isr=1"
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
	public static Consumer<String, AltinnEventMelding> consumer;

	private Producer<String, AltinnEventMelding> producer;

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		producer = producer();
		consumer = setupKafkaConsumer();
	}

	protected Producer<String, AltinnEventMelding> producer() {
		Map<String, Object> configs = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));

		JsonSerializer<AltinnEventMelding> valueSerializer = new JsonSerializer<>(objectMapper);
		valueSerializer.setAddTypeInfo(false);
		return new DefaultKafkaProducerFactory<>(configs, new StringSerializer(), valueSerializer).createProducer();
	}

	protected Consumer<String, AltinnEventMelding> setupKafkaConsumer() {
		Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("itest-group", "true", embeddedKafkaBroker);
		consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

		consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
		consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, AltinnEventMelding.class.getName());
		consumerProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
		consumer = new DefaultKafkaConsumerFactory<String, AltinnEventMelding>(consumerProps).createConsumer();

		consumer.subscribe(singletonList(PRIVAT_ALTINN_MELDING_TOPIC));
		return consumer;
	}

	protected void sendToInnTopic(AltinnEventMelding altinnEventMelding) {
		producer.send(new ProducerRecord<>(PRIVAT_ALTINN_MELDING_TOPIC, altinnEventMelding));
		producer.flush();
	}
}
