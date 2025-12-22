package org.example.chain.domain.post.data.res;

import org.example.chain.domain.post.entity.Post;
import org.example.chain.domain.post.entity.PostComment;

import java.time.Instant;

public record PostCommentReadRes(
        Long commentId,
        Long userId,
        String userName,
        String content,
        Instant createAt
) {
    public static PostCommentReadRes from(PostComment comment) {
        return new PostCommentReadRes(
                comment.getId(),
                comment.getUser().getId(),
                comment.getUser().getName(),
                comment.getContent(),
                comment.getCreateAt()
        );
    }
}
