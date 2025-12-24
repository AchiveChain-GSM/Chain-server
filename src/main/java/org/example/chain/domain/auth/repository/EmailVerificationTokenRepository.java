package org.example.chain.domain.auth.repository;

import org.example.chain.domain.auth.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationTokenRepository  extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByToken(String token);
    Optional<EmailVerificationToken> findByEmail(String email);
    Optional<EmailVerificationToken> findByTokenAndEmail(String token, String email);
}