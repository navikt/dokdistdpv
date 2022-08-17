package no.nav.dokdistdpv.qdist016.itest;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;

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
	public BrokerService broker() {
		BrokerService service = new BrokerService();
		service.setPersistent(false);
		return service;
	}

	@Bean
	public ConnectionFactory activemqConnectionFactory() {
		ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("vm://localhost?create=false");
		RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
		redeliveryPolicy.setMaximumRedeliveries(0);
		activeMQConnectionFactory.setRedeliveryPolicy(redeliveryPolicy);
		return activeMQConnectionFactory;
	}
}
