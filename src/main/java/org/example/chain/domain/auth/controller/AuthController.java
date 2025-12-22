package org.example.chain.domain.auth.controller;

import lombok.RequiredArgsConstructor;
import org.example.chain.domain.auth.data.request.ChangePasswordReq;
import org.example.chain.domain.auth.data.request.LoginReq;
import org.example.chain.domain.auth.data.request.SendEmailReq;
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
    public void sendEmail(@RequestBody SendEmailReq request){
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        String token = emailService.createVerificationToken(user);
        emailService.sendVerificationEmail(user.getEmail(), token);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordReq request){
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        user.update(passwordEncoder.encode(request.password()));
        return ResponseEntity.created(java.net.URI.create("/api/auth/login")).build();
    }
}