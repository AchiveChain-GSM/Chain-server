package org.example.chain.domain.post.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
public class PostView {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "postView_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = true)
    private Post post;

    @Column(name = "createAt")
    private Instant createAt;
}
