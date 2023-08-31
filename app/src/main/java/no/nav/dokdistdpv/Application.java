package no.nav.dokdistdpv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import static java.lang.System.getenv;
import static java.lang.System.setProperty;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Application {
	public static void main(String[] args) {
		setProperty("javax.net.ssl.keyStorePassword", getenv("DOKDISTDPVCERT_KEYSTORE_PASSWORD"));
		SpringApplication.run(Application.class, args);
	}
}
