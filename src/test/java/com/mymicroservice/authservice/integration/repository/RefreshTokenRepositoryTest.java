package com.mymicroservice.authservice.integration.repository;

import com.mymicroservice.authservice.configuration.AbstractContainerTest;
import com.mymicroservice.authservice.model.RefreshToken;
import com.mymicroservice.authservice.repository.RefreshTokenRepository;
import com.mymicroservice.authservice.util.RefreshTokenGenerator;
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
class RefreshTokenRepositoryTest extends AbstractContainerTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshToken expectedRefreshToken;

    @BeforeEach
    void init() {
        refreshTokenRepository.deleteAll();
        expectedRefreshToken = refreshTokenRepository.save(RefreshTokenGenerator.generateRefreshToken());
    }

    @Test
    void findByUserEmailIgnoreCase_ShouldReturnRefreshToken_WhenEmailExists() {
        Optional<RefreshToken> actualRefreshToken = refreshTokenRepository.findByUserEmailIgnoreCase(
                expectedRefreshToken.getUserEmail());

        assertNotNull(actualRefreshToken.orElse(null));
        assertEquals(expectedRefreshToken, actualRefreshToken.get());
        assertThat(actualRefreshToken).isPresent().contains(expectedRefreshToken);
    }

    @Test
    void findByUserEmailIgnoreCase_ShouldReturnEmpty_WhenEmailNotExists() {
        Optional<RefreshToken> actualRefreshToken = refreshTokenRepository.findByUserEmailIgnoreCase(
                NON_EXISTING_EMAIL);

        assertFalse(actualRefreshToken.isPresent());
    }

    @Test
    void deleteRefreshTokenByUserEmailIgnoreCase_ShouldDeleteRefreshToken_WhenEmailExists() {
        refreshTokenRepository.deleteRefreshTokenByUserEmailIgnoreCase(expectedRefreshToken.getUserEmail());

        Optional<RefreshToken> actualRefreshToken = refreshTokenRepository.findByUserEmailIgnoreCase(
                expectedRefreshToken.getUserEmail());

        assertFalse(actualRefreshToken.isPresent());
    }
}
