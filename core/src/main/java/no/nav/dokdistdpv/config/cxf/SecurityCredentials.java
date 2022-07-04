package no.nav.dokdistdpv.config.cxf;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotBlank;
import java.util.Properties;

/**
 * @param securityFile             Virksomhetens sertifikat (format PKCS12)
 * @param securityPassword         Passord tilhørende sertifikat
 * @param securityAlias            Alias tilhørende sertifikat (aka. friendly name)
 */
@ConfigurationProperties(prefix = "virksomhetssertifikat")
public record SecurityCredentials(
		@NotBlank String securityFile,
		@NotBlank String securityPassword,
		@NotBlank String securityAlias) {

	public static final Properties keyStoreProperties = new Properties();

	public SecurityCredentials {
		keyStoreProperties.setProperty("org.apache.ws.security.crypto.provider", "org.apache.ws.security.components.crypto.Merlin");
		keyStoreProperties.setProperty("org.apache.ws.security.crypto.merlin.keystore.file", securityFile);
		keyStoreProperties.setProperty("org.apache.ws.security.crypto.merlin.keystore.password", securityPassword);
		keyStoreProperties.setProperty("org.apache.ws.security.crypto.merlin.keystore.type", "pkcs12");
		keyStoreProperties.setProperty("org.apache.ws.security.crypto.merlin.keystore.private.password", securityPassword);
		keyStoreProperties.setProperty("org.apache.ws.security.crypto.merlin.keystore.alias", securityAlias);
	}

}
