package com.mymicroservice.authservice.repository;

import com.mymicroservice.authservice.configuration.TestContainersConfig;
import com.mymicroservice.authservice.model.UserCredential;
import com.mymicroservice.authservice.repositiry.UserCredentialRepository;
import com.mymicroservice.authservice.util.UserCredentialGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@Slf4j
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // Disabling DataSource Replacement
@Import(TestContainersConfig.class)
public class UserCredentialRepositoryTest {

    @Autowired
    private UserCredentialRepository userRepository;

    private static UserCredential expectedUser;

    @BeforeAll
    static void setUp(){
        expectedUser = UserCredentialGenerator.generateUser();
    }

    @BeforeEach
    void init() {
        userRepository.deleteAll();
        expectedUser = userRepository.save(expectedUser);
    }

    @Test
    void findByEmail_shouldReturnUserWhenExists() {
        Optional<UserCredential> actualUser = userRepository.findByEmailIgnoreCase(expectedUser.getEmail());
        log.info("Test to find the User with email: {} "+expectedUser.getEmail());

        assertNotNull(actualUser.get());
        assertEquals(expectedUser, actualUser.get());
        assertThat(actualUser).isPresent().contains(expectedUser);
    }

    @Test
    public void findByEmail_shouldReturnEmptyWhenNotExists() {
        Optional<UserCredential> actualUser = userRepository.findByEmailIgnoreCase("non-existing-email");

        assertFalse(actualUser.isPresent());
    }
}
