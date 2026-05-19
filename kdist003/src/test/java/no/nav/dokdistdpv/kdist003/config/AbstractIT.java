package no.nav.dokdistdpv.kdist003.config;

import tools.jackson.databind.json.JsonMapper;
import lombok.SneakyThrows;
import no.altinn.event.domain.CloudEvent;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.EnableWireMock;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@EnableAutoConfiguration
@SpringBootTest(
		classes = {ApplicationTestConfig.class},
		webEnvironment = NONE
)
@EnableWireMock
@EmbeddedKafka(partitions = 1, topics = {"altinn-melding-hendelse"},
		brokerProperties = {
				"offsets.topic.replication.factor=1",
		})
@ActiveProfiles("itest")
public abstract class AbstractIT {

	protected static final String PRIVAT_ALTINN_MELDING_TOPIC = "altinn-melding-hendelse";
	public static final String FINN_FORSENDELSE_PATH = "/administrerforsendelse/finnforsendelse/konversasjonsId/af0e7e0c-579c-4563-9398-10cdf031b80d";
	public static final String HENT_FORSENDELSE_PATH = "/administrerforsendelse";
	public static final String OPPDATER_FORSENDELSE_PATH = "/administrerforsendelse/oppdaterforsendelse";
	public static final String OPPDATERDISTRIBUSJONSINFO_URL = "/dokarkiv/journalpostapi/v1/journalpost/\\d+/oppdaterDistribusjonsinfo";
	public static final String DISTRIBUERTILPRINT_PATH = "/administrerforsendelse/distribuertilnykanal";


	@Autowired
	protected JsonMapper jsonMapper;
	@Autowired
	protected EmbeddedKafkaBroker embeddedKafkaBroker;
	@Autowired
	protected KafkaTemplate<String, String> kafkaTemplate;
	@Autowired
	private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

	@BeforeEach
	void waitForKafkaConsumerAssignment() {
		for (MessageListenerContainer container : kafkaListenerEndpointRegistry.getListenerContainers()) {
			ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
		}
	}

	@SneakyThrows
	protected void sendToInnTopic(CloudEvent cloudEvent) {
		kafkaTemplate.send(PRIVAT_ALTINN_MELDING_TOPIC, jsonMapper.writeValueAsString(cloudEvent))
				.get(10, TimeUnit.SECONDS);
	}
}
