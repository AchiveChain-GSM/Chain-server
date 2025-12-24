package org.example.chain.domain.auth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.chain.domain.auth.data.request.ChangePasswordReq;
import org.example.chain.domain.auth.data.request.LoginReq;
import org.example.chain.domain.auth.data.request.SendEmailReq;
import org.example.chain.domain.auth.data.request.VerifyEmailReq;
import org.example.chain.domain.auth.data.response.TokenRes;
import org.example.chain.domain.auth.service.AuthService;
import org.example.chain.domain.auth.service.EmailService;
import org.example.chain.domain.user.data.request.SignUpReq;
import org.example.chain.domain.user.entity.User;
import org.example.chain.domain.user.repository.UserRepository;
import org.example.chain.domain.user.service.UserService;
import org.example.chain.global.security.util.SecurityUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final EmailService emailService;
    private final AuthService authService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/sign-up")
    public ResponseEntity<Void> signUp(@RequestBody SignUpReq request) {
        userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<TokenRes> login(@RequestBody LoginReq request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/send-email")
    public ResponseEntity<HttpStatus> sendEmail(@RequestBody SendEmailReq request){

        log.info("request.email = {}", request.email());

        String token = emailService.createVerificationToken(request.email());
        emailService.sendVerificationEmail(request.email(), token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestBody VerifyEmailReq request) {
        authService.verifyEmail(request.token());
        return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
    }

    @PostMapping("/change-password")
    public ResponseEntity<URI> changePassword(@RequestBody ChangePasswordReq request){
        User user = securityUtil.getCurrentUser();

        user.update(passwordEncoder.encode(request.password()));
        return ResponseEntity.ok(java.net.URI.create("/api/auth/login"));
    }
}