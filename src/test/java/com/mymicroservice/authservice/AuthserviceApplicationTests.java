package com.mymicroservice.authservice;

import com.mymicroservice.authservice.configuration.TestContainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestContainersConfig.class})
@Testcontainers(disabledWithoutDocker = true)
class AuthserviceApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void testDataSource() {
		System.out.println("PostgreSQL JDBC URL: " + TestContainersConfig.postgreSQLContainer.getJdbcUrl());
	}

}
