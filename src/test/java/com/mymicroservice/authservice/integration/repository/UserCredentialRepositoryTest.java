package com.mymicroservice.authservice.integration.repository;

import com.mymicroservice.authservice.configuration.AbstractContainerTest;
import com.mymicroservice.authservice.model.UserCredential;
import com.mymicroservice.authservice.repository.UserCredentialRepository;
import com.mymicroservice.authservice.util.UserCredentialGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static com.mymicroservice.authservice.util.data.TestConstants.NON_EXISTING_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class UserCredentialRepositoryTest extends AbstractContainerTest {

    @Autowired
    private UserCredentialRepository userRepository;

    private UserCredential expectedUser;

    @BeforeEach
    void init() {
        userRepository.deleteAll();
        expectedUser = userRepository.save(UserCredentialGenerator.generateUser());
    }

    @Test
    void findByEmailIgnoreCase_ShouldReturnUser_WhenEmailExists() {
        Optional<UserCredential> actualUser = userRepository.findByEmailIgnoreCase(expectedUser.getEmail());

        assertNotNull(actualUser.orElse(null));
        assertEquals(expectedUser, actualUser.get());
        assertThat(actualUser).isPresent().contains(expectedUser);
    }

    @Test
    void findByEmailIgnoreCase_ShouldReturnEmpty_WhenEmailNotExists() {
        Optional<UserCredential> actualUser = userRepository.findByEmailIgnoreCase(NON_EXISTING_EMAIL);

        assertFalse(actualUser.isPresent());
    }
}
