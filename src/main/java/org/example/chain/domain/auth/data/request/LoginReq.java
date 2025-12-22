package org.example.chain.domain.auth.data.request;

public record LoginReq(
        String email,
        String password
) {}
