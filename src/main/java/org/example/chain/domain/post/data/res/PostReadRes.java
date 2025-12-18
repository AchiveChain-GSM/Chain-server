package org.example.chain.domain.post.data.res;

import lombok.Builder;
import org.example.chain.domain.post.entity.Post;

import java.time.Instant;
import java.util.List;

@Builder
public record PostReadRes(
        Long id,
        String title,
        String content,
        Instant createAt,
        String name,
        Long likes,
        Long comments,
        List<String> tags) {

    public static PostReadRes from(Post post) {
        return new PostReadRes(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getCreateAt(),
                post.getUser().getName(),
                post.getLikes(),
                post.getComments(),
                post.getPostTags().stream().map(postTag -> postTag.getTag().getName()).toList()
        );
    }
}
