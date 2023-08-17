package no.nav.dokdistdpv.jms;

import com.ibm.mq.jakarta.jms.MQConnectionFactory;
import com.ibm.mq.jakarta.jms.MQQueue;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.Queue;
import no.nav.dokdistdpv.properties.JmsQueueProperties;
import no.nav.dokdistdpv.properties.ServiceuserProperties;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;

import static com.ibm.mq.constants.CMQC.MQENC_NATIVE;
import static com.ibm.msg.client.jakarta.jms.JmsConstants.JMS_IBM_CHARACTER_SET;
import static com.ibm.msg.client.jakarta.jms.JmsConstants.JMS_IBM_ENCODING;
import static com.ibm.msg.client.jakarta.jms.JmsConstants.USERID;
import static com.ibm.msg.client.jakarta.wmq.common.CommonConstants.WMQ_CM_CLIENT;

@Configuration
@Profile({"nais", "local"})
public class JmsConfig {
	private static final int UTF_8_WITH_PUA = 1208;

	@Bean
	public Queue qdist016(JmsQueueProperties jmsQueueProperties) throws JMSException {
		return new MQQueue(jmsQueueProperties.getQueues().getQdist016());
	}

	@Bean
	public Queue qdist016TekniskFeil(JmsQueueProperties jmsQueueProperties) throws JMSException {
		return new MQQueue(jmsQueueProperties.getQueues().getQdist016TekniskFeil());
	}

	@Bean
	public Queue qdist016FunksjonellFeil(JmsQueueProperties jmsQueueProperties) throws JMSException {
		return new MQQueue(jmsQueueProperties.getQueues().getQdist016FunksjonellFeil());
	}

	@Bean
	public Queue qdist009(JmsQueueProperties jmsQueueProperties) throws JMSException {
		return new MQQueue(jmsQueueProperties.getQueues().getQdist009());
	}

	@Bean
	public ConnectionFactory connectionFactory(final JmsQueueProperties jmsQueueProperties,
											   final ServiceuserProperties serviceuserProperties) throws JMSException {
		return createConnectionFactory(jmsQueueProperties, serviceuserProperties);
	}

	private JmsPoolConnectionFactory createConnectionFactory(final JmsQueueProperties jmsQueueProperties,
															 final ServiceuserProperties serviceuserProperties) throws JMSException {
		MQConnectionFactory mqConnectionFactory = new MQConnectionFactory();

		mqConnectionFactory.setHostName(jmsQueueProperties.getBroker().getHostname());
		mqConnectionFactory.setPort(jmsQueueProperties.getBroker().getPort());
		mqConnectionFactory.setChannel(jmsQueueProperties.getBroker().getChannel());
		mqConnectionFactory.setQueueManager(jmsQueueProperties.getBroker().getName());
		mqConnectionFactory.setTransportType(WMQ_CM_CLIENT);
		mqConnectionFactory.setCCSID(UTF_8_WITH_PUA);
		mqConnectionFactory.setIntProperty(JMS_IBM_ENCODING, MQENC_NATIVE);
		mqConnectionFactory.setIntProperty(JMS_IBM_CHARACTER_SET, UTF_8_WITH_PUA);
		mqConnectionFactory.setStringProperty(USERID, serviceuserProperties.getUsername());

		UserCredentialsConnectionFactoryAdapter adapter = new UserCredentialsConnectionFactoryAdapter();
		adapter.setTargetConnectionFactory(mqConnectionFactory);
		adapter.setUsername(serviceuserProperties.getUsername());
		adapter.setPassword(serviceuserProperties.getPassword());

		JmsPoolConnectionFactory pooledFactory = new JmsPoolConnectionFactory();
		pooledFactory.setConnectionFactory(adapter);
		pooledFactory.setMaxConnections(10);
		pooledFactory.setMaxSessionsPerConnection(10);
		return pooledFactory;
	}
}
