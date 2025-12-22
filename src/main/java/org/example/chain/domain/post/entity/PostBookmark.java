package org.example.chain.domain.post.entity;

import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import org.example.chain.domain.user.entity.User;

@Entity
@RequiredArgsConstructor
public class PostBookmark {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    public PostBookmark(Post post, User user){
        this.post = post;
        this.user = user;
    }
}
