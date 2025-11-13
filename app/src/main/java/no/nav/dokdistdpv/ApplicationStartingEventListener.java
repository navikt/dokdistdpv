package no.nav.dokdistdpv;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Slf4j
@Component
public class ApplicationStartingEventListener implements ApplicationListener<ApplicationStartingEvent> {

	@Override
	public void onApplicationEvent(ApplicationStartingEvent event) {
		altinn2DecodeBase64Virksomhetssertifikat();
	}

	private void altinn2DecodeBase64Virksomhetssertifikat() {
		String base64EncodedFilename = System.getenv("NAV_VIRKSOMHETSSERTIFIKAT_KEY");
		Path base64EncodedPath = Paths.get(base64EncodedFilename);
		if (!Files.exists(base64EncodedPath)) {
			throw new IllegalArgumentException("NAV_VIRKSOMHETSSERTIFIKAT_KEY må peke på en fil");
		}

		Path base64DecodedPath = Paths.get(base64EncodedFilename.replace(".b64", ""));
		try (InputStream base64EncodedInputStream = new FileSystemResource(base64EncodedPath).getInputStream()) {
			try(InputStream base64DecodedInputStream = Base64.getDecoder().wrap(base64EncodedInputStream)) {
				Files.copy(base64DecodedInputStream, base64DecodedPath, REPLACE_EXISTING);
			}
		} catch (IOException e) {
			throw new IllegalArgumentException("Klarte ikke skrive fil", e);
		}

		System.setProperty("nav.virksomhetssertifikat.path", "file://" + base64DecodedPath.toAbsolutePath());
		log.info("Dekodet base64 sertifikatfil og satt virksomhetssertifikat.path");
	}
}