package org.example.chain.domain.post.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
public class PostLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "postLike_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = true)
    private Post post;

    @Column(name = "createAt")
    private Instant createAt;
}
