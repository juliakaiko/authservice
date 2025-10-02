package com.mymicroservice.authservice;

import com.mymicroservice.authservice.configuration.AbstractContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AuthserviceApplicationTests extends AbstractContainerTest{

	@Test
	void contextLoads() {
	}

}
