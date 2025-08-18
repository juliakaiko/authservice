package com.mymicroservice.authservice.repository;

import com.mymicroservice.authservice.configuration.TestContainersConfig;
import com.mymicroservice.authservice.model.RefreshToken;
import com.mymicroservice.authservice.repositiry.RefreshTokenRepository;
import com.mymicroservice.authservice.util.RefreshTokenGenerator;
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
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

@Slf4j
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // Disabling DataSource Replacement
@Import(TestContainersConfig.class)
public class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private static RefreshToken expectedRefreshToken;

    @BeforeAll
    static void setUp(){
        expectedRefreshToken = RefreshTokenGenerator.generateRefreshToken();
    }

    @BeforeEach
    void init() {
        refreshTokenRepository.deleteAll();
        expectedRefreshToken = refreshTokenRepository.save(expectedRefreshToken);
    }

  @Test
    void findByUserEmail_shouldReturnRefreshTokenWhenExists() {
        Optional<RefreshToken> actualRefreshToken = refreshTokenRepository.findByUserEmailIgnoreCase(
                expectedRefreshToken.getUserEmail());
        log.info("Test to find the RefreshToken by user_email: {} " + expectedRefreshToken.getUserEmail());

        assertNotNull(actualRefreshToken.get());
        assertEquals(expectedRefreshToken, actualRefreshToken.get());
        assertThat(actualRefreshToken).isPresent().contains(expectedRefreshToken);
    }

    @Test
    public void findByUserEmail_shouldReturnEmptyWhenNotExists() {
        Optional<RefreshToken> actualRefreshToken = refreshTokenRepository.findByUserEmailIgnoreCase(
                "non-existing-email");

        assertFalse(actualRefreshToken.isPresent());
    }

    @Test
    public void deleteByUserEmail_shouldDeleteRefreshToken() {
        refreshTokenRepository.deleteRefreshTokenByUserEmailIgnoreCase(expectedRefreshToken.getUserEmail());
        log.info("Test to delete the RefreshToken by user_email: {} " + expectedRefreshToken.getUserEmail());
        Optional<RefreshToken> actualRefreshToken = refreshTokenRepository.findByUserEmailIgnoreCase(
                expectedRefreshToken.getUserEmail());

        assertThrows(NoSuchElementException.class, () -> actualRefreshToken.get());
        assertFalse(actualRefreshToken.isPresent());
    }
}
