package org.example.chain.domain.user.data.request;

public record SignUpReq(
        String email,
        String password,
        String userName, String generation, String userClass, String userNumber,
        String role) {
}
