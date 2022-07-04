package no.nav.dokdistdpv.config.cxf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ContextConfiguration;

import java.security.Provider;


//@Profile({"itest"})
class AltinnClientTest {

	@Mock
	private ServiceCode serviceCode;

	@InjectMocks
	private AltinnClient altinnClient;

	@Test
	void shouldInsertCorrespondence() {
		System.out.println("Hei");
	}
}