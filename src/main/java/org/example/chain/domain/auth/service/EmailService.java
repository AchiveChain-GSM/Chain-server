package org.example.chain.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.chain.domain.auth.data.request.VerifyEmailReq;
import org.example.chain.domain.auth.entity.EmailVerificationToken;
import org.example.chain.domain.auth.repository.EmailVerificationTokenRepository;
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
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Transactional
        public String createVerificationToken(String email) {
            String token = UUID.randomUUID().toString();

            EmailVerificationToken verificationToken =
                    EmailVerificationToken.builder()
                            .token(token)
                            .email(email)
                            .verified(false)
                            .expiryDate(Instant.now().plus(1, ChronoUnit.DAYS))
                            .build();

            tokenRepository.save(verificationToken);
            return token;
        }

    @Transactional
    public void verifyToken(VerifyEmailReq request) {
        EmailVerificationToken verificationToken =
                tokenRepository.findByTokenAndEmail(request.token(), request.email())
                        .orElseThrow(() -> new IllegalArgumentException("해당 이메일과 인증코드의 정보가 일치하지 않습니다."));

        if (verificationToken.getExpiryDate().isBefore(Instant.now())) {
            throw new IllegalArgumentException("만료된 토큰");
        }

        emailVerificationTokenRepository.delete(verificationToken);
    }

    public void sendVerificationEmail(String toEmail, String token) {
        String subject = "[Chain] 이메일 인증 안내";

        String content = """
                안녕하세요.
                
                아래 문자를 이용하여 이메일 인증을 완료해주세요.
                
                %s
                
                감사합니다.
                """.formatted(token);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(content);

        mailSender.send(message);
    }
}