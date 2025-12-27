package org.example.chain.domain.post.data.res;

import lombok.Builder;
import org.example.chain.domain.post.entity.Post;

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
        List<PostCommentReadRes> comments)
{
    public static PostDetailReadRes from (Post post, Map<Long, String> images) {
        return new PostDetailReadRes(
                post.getId(),
                post.getTitle(),
                post.getUser().getId(),
                post.getUser().getName(),
                post.getCreateAt(),
                post.getContent(),
                post.getPostTags().stream().map(postTag -> postTag.getTag().getName()).toList(),
                images,
                post.getLikes(),
                post.getBookmarks(),
                post.getViews(),
                post.getPostComments().stream()
                        .map(postComment -> {
                            return PostCommentReadRes.from(postComment);
                        }).toList());
    }
}
