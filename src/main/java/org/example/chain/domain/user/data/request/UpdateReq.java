package org.example.chain.domain.user.data.request;

import org.example.chain.domain.user.enums.Authority;

import java.util.List;

public record UpdateReq(String email, String password, List<Authority> roles) {
}
