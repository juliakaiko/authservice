package com.mymicroservice.authservice.integration;

import com.mymicroservice.authservice.configuration.AbstractContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class AuthserviceApplicationTests extends AbstractContainerTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads_ShouldStartApplicationContext_WhenTestProfileActive() {
        assertNotNull(applicationContext);
    }
}
