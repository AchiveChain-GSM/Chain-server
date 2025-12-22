package org.example.chain.domain.auth.data.response;

public record TokenRes(
        String accessToken,
        String refreshToken
) {}