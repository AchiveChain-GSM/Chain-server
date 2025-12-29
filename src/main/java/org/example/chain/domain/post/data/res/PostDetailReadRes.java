package org.example.chain.domain.post.data.res;

import lombok.Builder;
import org.example.chain.domain.post.entity.Post;
import org.example.chain.domain.post.entity.PostComment;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Builder
public record PostDetailReadRes(
        Long id,
        String title,
        Long authorId,
        String author,
        Instant createAt,
        String content,
        List<String> tags,
        Map<Long, String> images,
        Long likes,
        Long bookmarks,
        Long views,
        List<PostCommentReadRes> comments,
        boolean isLiked,
        boolean isBookmarked
) {

    public static PostDetailReadRes from(
            Post post,
            List<PostComment> comments,      // 🔥 댓글은 외부에서 주입
            Map<Long, String> images,
            boolean isLiked,
            boolean isBookmarked,
            long likes,
            long bookmarks
    ) {
        return PostDetailReadRes.builder()
                .id(post.getId())
                .title(post.getTitle())
                .authorId(post.getUser().getId())
                .author(post.getUser().getName())
                .createAt(post.getCreateAt())
                .content(post.getContent())
                .tags(
                        post.getPostTags()
                                .stream()
                                .map(pt -> pt.getTag().getName())
                                .distinct()
                                .toList()
                )
                .images(images)
                .likes(likes)
                .bookmarks(bookmarks)
                .views(post.getViews())
                .comments(
                        comments.stream()
                                .map(PostCommentReadRes::from)
                                .toList()
                )
                .isLiked(isLiked)
                .isBookmarked(isBookmarked)
                .build();
    }
}
