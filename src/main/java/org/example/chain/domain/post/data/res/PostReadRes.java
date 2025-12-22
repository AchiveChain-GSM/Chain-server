package org.example.chain.domain.post.data.res;

import lombok.Builder;
import org.example.chain.domain.post.entity.Post;

import java.util.List;

@Builder
public record PostReadRes(String title, String author, String content, List<String> tags, String firstImageUrl) {

    public static PostReadRes from(Post post, String firstImageUrl) {
        return new PostReadRes(
                post.getTitle(),
                post.getUser().getName(),
                post.getContent(),
                post.getPostTags().stream().map(postTag -> postTag.getTag().getName()).toList(),
                firstImageUrl
        );
    }
}
