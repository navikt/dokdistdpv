package no.nav.dokdistdpv.kdist003.config;

import no.nav.dokdistdpv.certificate.KeyStoreProperties;
import no.nav.dokdistdpv.cloudstorage.EncryptedBucketStorage;
import no.nav.dokdistdpv.config.AzureConfig;
import no.nav.dokdistdpv.config.SafGraphQLConfig;
import no.nav.dokdistdpv.consumer.altinn2.Altinn2Client;
import no.nav.dokdistdpv.properties.AltinnProperties;
import no.nav.dokdistdpv.properties.DokdistdpvProperties;
import no.nav.dokdistdpv.properties.JmsQueueProperties;
import no.nav.dokdistdpv.properties.NaisProperties;
import no.nav.dokdistdpv.properties.ServiceuserProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import static org.mockito.Mockito.mock;

@Configuration
@Profile("itest")
@EnableConfigurationProperties({
		AzureConfig.class,
		ServiceuserProperties.class,
		DokdistdpvProperties.class,
		SafGraphQLConfig.class,
		AltinnProperties.class,
		KeyStoreProperties.class,
		JmsQueueProperties.class,
		NaisProperties.class
})
@Import({
		Altinn2Client.class,
		LocalTestCacheConfig.class
})
@ComponentScan(basePackages = "no.nav.dokdistdpv")
public class ApplicationTestConfig {

	@Bean
	public EncryptedBucketStorage bucketStorage() {
		return mock(EncryptedBucketStorage.class);
	}

	@Bean
	public CommonErrorHandler noRetryErrorHandler() {
		return new DefaultErrorHandler(new FixedBackOff(0L, 0L));
	}

}
