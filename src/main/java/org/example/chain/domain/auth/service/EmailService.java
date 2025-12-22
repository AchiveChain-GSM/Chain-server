package org.example.chain.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.chain.domain.auth.entity.EmailVerificationToken;
import org.example.chain.domain.auth.repository.EmailVerificationTokenRepository;
import org.example.chain.domain.user.entity.User;
import org.example.chain.domain.user.repository.UserRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor

public class EmailService {
    private final JavaMailSender mailSender;
    private final EmailVerificationTokenRepository tokenRepository;

    @Transactional
    public String createVerificationToken(User user) {
        String token = UUID.randomUUID().toString();

        EmailVerificationToken verificationToken =
                EmailVerificationToken.builder()
                        .token(token)
                        .user(user)
                        .expiryDate(Instant.now().plus(1, ChronoUnit.DAYS))
                        .build();

        tokenRepository.save(verificationToken);
        return token;
    }

    @Transactional
    public void verifyToken(String token) {
        EmailVerificationToken verificationToken =
                tokenRepository.findByToken(token)
                        .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 토큰"));

        if (verificationToken.getExpiryDate().isBefore(Instant.now())) {
            throw new IllegalArgumentException("만료된 토큰");
        }

        User user = verificationToken.getUser();
        user.verifyEmail();

        tokenRepository.delete(verificationToken);
    }

    public void sendVerificationEmail(String toEmail, String token) {
        String subject = "[Chain] 이메일 인증 안내";
        String verificationUrl =
                "http://localhost:8080/api/auth/verify-email?token=" + token;

        String content = """
                안녕하세요.
                
                아래 링크를 클릭하여 이메일 인증을 완료해주세요.
                
                %s
                
                감사합니다.
                """.formatted(verificationUrl);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(content);

        mailSender.send(message);
    }
}