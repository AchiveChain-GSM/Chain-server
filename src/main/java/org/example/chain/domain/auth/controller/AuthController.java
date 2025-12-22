package org.example.chain.domain.auth.controller;

import lombok.RequiredArgsConstructor;
import org.example.chain.domain.auth.data.request.LoginReq;
import org.example.chain.domain.auth.data.response.TokenRes;
import org.example.chain.domain.auth.service.AuthService;
import org.example.chain.domain.user.data.request.SignUpReq;
import org.example.chain.domain.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/sign-up")
    public ResponseEntity<Void> signUp(@RequestBody SignUpReq request) {
        userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<TokenRes> login(@RequestBody LoginReq request) {
        return ResponseEntity.ok(authService.login(request));
    }
}