package com.epam.training.microservices.audio.resources;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;

@SpringBootTest
class ResourcesApplicationTest {

	@MockBean
	private WebSecurityConfiguration springSecurityFilterChain;

	@Test
	void contextLoads() {
	}

}
