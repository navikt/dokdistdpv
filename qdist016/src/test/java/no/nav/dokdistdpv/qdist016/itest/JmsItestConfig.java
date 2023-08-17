package no.nav.dokdistdpv.qdist016.itest;


import jakarta.jms.ConnectionFactory;
import jakarta.jms.Queue;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQQueue;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


@Configuration
@Profile("itest")
public class JmsItestConfig {

	@Bean
	public Queue qdist016(@Value("${dokdistdpi_qdist016_dist_til_dpv.queuename}") String qdist016QueueName) {
		return new ActiveMQQueue(qdist016QueueName);
	}

	@Bean
	public Queue qdist016FunksjonellFeil(@Value("${dokdistdpi_qdist016_funk_feil.queuename}") String qdist016FunksjonellFeilQueueName) {
		return new ActiveMQQueue(qdist016FunksjonellFeilQueueName);
	}

	@Bean
	public Queue qdist016TekniskFeil(@Value("${dokdistdpi_qdist016_boq.queuename}") String qdist016TekniskFeilQueueName) {
		return new ActiveMQQueue(qdist016TekniskFeilQueueName);
	}

	@Bean(initMethod = "start", destroyMethod = "stop")
	public EmbeddedActiveMQ activeMQServer() {
		EmbeddedActiveMQ embeddedActiveMQ = new EmbeddedActiveMQ();
		embeddedActiveMQ.setConfigResourcePath("artemis-server.xml");
		return embeddedActiveMQ;
	}

	@Bean
	public ConnectionFactory activemqConnectionFactory(EmbeddedActiveMQ embeddedActiveMQ) {
		ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("vm://0");
		JmsPoolConnectionFactory pooledFactory = new JmsPoolConnectionFactory();
		pooledFactory.setConnectionFactory(activeMQConnectionFactory);
		pooledFactory.setMaxConnections(1);
		return pooledFactory;
	}
}
