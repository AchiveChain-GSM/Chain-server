package org.example.chain.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.chain.domain.auth.data.request.LoginReq;
import org.example.chain.domain.auth.data.response.TokenRes;
import org.example.chain.domain.user.entity.CustomUserDetails;
import org.example.chain.domain.user.entity.User;
import org.example.chain.domain.user.repository.UserRepository;
import org.example.chain.global.security.auth.jwt.JwtProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional(readOnly = true)
    public TokenRes login(LoginReq request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 잘못되었습니다."));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 잘못되었습니다.");
        }

        CustomUserDetails userDetails = new CustomUserDetails(user);

        String accessToken = jwtProvider.createAccessToken(
                user.getEmail(),
                userDetails.getAuthorities()
        );

        String refreshToken = jwtProvider.createRefreshToken(user.getEmail());

        return new TokenRes(accessToken, refreshToken);
    }

    @Transactional
    public void verifyEmail(String token) {
        emailService.verifyToken(token);
    }
}