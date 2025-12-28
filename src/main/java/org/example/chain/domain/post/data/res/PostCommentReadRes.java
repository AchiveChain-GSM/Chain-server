package org.example.chain.domain.post.data.res;

import org.example.chain.domain.post.entity.PostComment;

import java.time.Instant;

public record PostCommentReadRes(
        Long id,
        String content,
        String writer,
        Instant createAt
) {
    public static PostCommentReadRes from(PostComment c) {
        return new PostCommentReadRes(
                c.getId(),
                c.getContent(),
                c.getUser().getName(),
                c.getCreateAt()
        );
    }
}