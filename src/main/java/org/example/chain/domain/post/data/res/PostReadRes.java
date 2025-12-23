package org.example.chain.domain.post.data.res;

import lombok.Builder;
import org.example.chain.domain.post.entity.Post;

import java.time.Instant;
import java.util.List;

@Builder
public record PostReadRes(
        Long postId,
        String title,
        String author,
        List<String> tags,
        String firstImageUrl,
        Instant createAt,
        Long likes,
        Long views,
        Long bookmarks,
        boolean isLiked,
        boolean isBookmarked) {

    public static PostReadRes from(Post post, String firstImageUrl, boolean isLiked, boolean isBookmarked) {
        return new PostReadRes(
                post.getId(),
                post.getTitle(),
                post.getUser().getName(),
                post.getPostTags().stream().map(postTag -> postTag.getTag().getName()).toList(),
                firstImageUrl,
                post.getCreateAt(),
                post.getLikes(),
                post.getViews(),
                post.getBookmarks(),
                isLiked,
                isBookmarked
        );
    }
}
