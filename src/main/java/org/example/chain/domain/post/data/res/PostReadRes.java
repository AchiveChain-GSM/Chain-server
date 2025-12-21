package org.example.chain.domain.post.data.res;

import lombok.Builder;
import org.example.chain.domain.post.entity.Post;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Builder
public record PostReadRes(
        Long id,
        String title,
        String content,
        List<PostBlockRes> contents,
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
                getContentsByPost(post),
                post.getCreateAt(),
                post.getUser().getName(),
                post.getLikes(),
                post.getComments(),
                post.getTags()
        );
    }


    static List<PostBlockRes> getContentsByPost(Post post) {

        List<PostBlockRes> postBlockResList = new ArrayList<>();

        for(var postBlock : post.getContents()){

            PostBlockRes postBlockRes = PostBlockRes.builder()
                    .sortOrder(postBlock.getSortOrder())
                    .blockType(postBlock.getBlockType())
                    .build();

            switch (postBlock.getBlockType()){
                case TEXT, H1, H2: {
                    postBlockRes.setTextBlockRes(
                            TextBlockRes.builder()
                                .textStyle(postBlock.getTextBlock().getTextStyleType())
                                .content(postBlock.getTextBlock().getContent())
                                .build()
                    );
                }break;

                case IMAGE: {
                    postBlockRes.setImageBlockRes(
                      ImageBlockRes.builder()
                              .imageUrl(postBlock.getImageBlock().getImageUrl())
                              .width(postBlock.getImageBlock().getWidth())
                              .height(postBlock.getImageBlock().getHeight())
                              .build()
                    );
                }break;

                case LIST: {
                    postBlockRes.setListBlockRes(
                      ListBlockRes.builder()
                              .listType(postBlock.getListBlock().getListBlock_type())
                              .contents(
                                      postBlock.getListBlock().getListItems().stream()
                                      .map(listItem -> {
                                          return ListItemRes.builder()
                                                  .textStyle(listItem.getTextStyleType())
                                                  .content(listItem.getContent())
                                                  .itemOrder(listItem.getItemOrder())
                                                  .build();
                                      }).toList()
                              ).build()
                    );
                }
            }

            postBlockResList.add(postBlockRes);

        }

        return postBlockResList;

    }
}
