package no.nav.dokdistdpv.qdist016.itest;


import jakarta.jms.ConnectionFactory;
import jakarta.jms.Queue;
import no.nav.dokdistdpv.properties.JmsQueueProperties;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQQueue;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;


@Configuration
@Profile("itest")
public class JmsItestConfig {

	@Bean
	public Queue qdist016(JmsQueueProperties jmsQueueProperties) {
		return new ActiveMQQueue(jmsQueueProperties.getQueues().getQdist016());
	}

	@Bean
	public Queue qdist016FunksjonellFeil(JmsQueueProperties jmsQueueProperties) {
		return new ActiveMQQueue(jmsQueueProperties.getQueues().getQdist016FunksjonellFeil());
	}

	@Bean
	public Queue qdist016TekniskFeil(JmsQueueProperties jmsQueueProperties) {
		return new ActiveMQQueue(jmsQueueProperties.getQueues().getQdist016TekniskFeil());
	}

	@Bean(initMethod = "start", destroyMethod = "stop")
	public EmbeddedActiveMQ embeddedActiveMQ() {
		EmbeddedActiveMQ embeddedActiveMQ = new EmbeddedActiveMQ();
		embeddedActiveMQ.setConfigResourcePath("artemis-server.xml");
		return embeddedActiveMQ;
	}

	@Bean
	@DependsOn("embeddedActiveMQ")
	public ConnectionFactory activemqConnectionFactory() {
		ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("vm://0");
		JmsPoolConnectionFactory pooledFactory = new JmsPoolConnectionFactory();
		pooledFactory.setConnectionFactory(activeMQConnectionFactory);
		pooledFactory.setMaxConnections(1);
		return pooledFactory;
	}
}
