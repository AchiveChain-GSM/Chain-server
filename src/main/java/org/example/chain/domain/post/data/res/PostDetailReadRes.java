package org.example.chain.domain.post.data.res;

import lombok.Builder;
import org.example.chain.domain.post.entity.Post;

import java.time.Instant;
import java.util.List;

@Builder
public record PostDetailReadRes(
        Long id,
        String title,
        String author,
        Instant createAt,
        List<PostBlockRes> contents,
        List<String> tags,
        List<String> images,
        Long likes,
        Long bookmarks,
        Long views,
        List<PostCommentReadRes> comments)
{
    public static PostDetailReadRes from (Post post, List<PostBlockRes> contents, List<String> images) {
        return new PostDetailReadRes( post.getId(), post.getTitle(),
                post.getUser().getName(),
                post.getCreateAt(),
                contents,
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
