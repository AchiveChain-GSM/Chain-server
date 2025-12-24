package org.example.chain.domain.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.chain.domain.user.entity.User;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private Instant expiryDate;

    private boolean verified;
}