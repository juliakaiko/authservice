package com.mymicroservice.authservice.repositiry;

import com.mymicroservice.authservice.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    void deleteRefreshTokenByUserEmailIgnoreCase(String email);

    Optional<RefreshToken> findByUserEmailIgnoreCase(String userEmail);

}
