package org.example.chain.domain.user.data.request;

import java.util.List;

public record UpdateReq(String email, String password, List<String> roles) {
}
