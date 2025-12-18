package org.example.chain.domain.post.data.res;

import lombok.Builder;
import org.example.chain.domain.post.entity.Post;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record PostReadRes(
        String title,
        String content,
        LocalDateTime createAt,
        Long likes,
        Long comments,
        List<String> tags) {
    public static PostReadRes from(Post post) {
        // Post 엔티티를 받아서 PostResponse DTO를 생성하여 반환합니다.
        return new PostReadRes(
                post.getId(),
                post.getTitle(),
                post.getCreateAt()
        );
    }
}
