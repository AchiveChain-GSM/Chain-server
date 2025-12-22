package org.example.chain.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.example.chain.domain.post.data.req.PostBlockReq;
import org.example.chain.domain.post.data.req.PostCreateReq;
import org.example.chain.domain.post.data.res.*;
import org.example.chain.domain.post.entity.*;
import org.example.chain.domain.post.enums.BlockType;
import org.example.chain.domain.post.repository.PostLikeRepository;
import org.example.chain.domain.post.repository.PostRepository;
import org.example.chain.domain.post.repository.PostViewRepository;
import org.example.chain.global.error.exception.PostNotFoundException;
import org.example.chain.global.s3.S3Service;
import org.example.chain.global.security.util.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.List;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final TagService tagService;
    private final SecurityUtil securityUtil;
    private final PostViewRepository postViewRepository;
    private final PostLikeRepository postLikeRepository;
    private final S3Service s3Service;

    @Transactional
    public Long createPost(PostCreateReq request){
        Post post = Post.builder()
                .title(request.title())
                .content(request.content())
                .user(securityUtil.getCurrentUser())
                .build();

        List<PostTag> postTags = request.tags().stream()
                .map(tagService::getOrCreateTag) // TagService의 메서드 호출
                .map(tag -> new PostTag(post, tag))
                .toList();

        post.getPostTags().addAll(postTags);

        int count = 1;
        for(var postBlockReg : request.blocks()) {

            PostBlock postBlock = PostBlock.builder()
                    .post(post)
                    .blockType(postBlockReg.blockType())
                    .sortOrder(count++)
                    .build();

            createBlockByType(postBlockReg.blockType(), postBlock, postBlockReg);

        }
        return postRepository.save(post).getId();
    }

    @Transactional
    void createBlockByType(BlockType blockType, PostBlock postBlock, PostBlockReq postBlockReq) {

        switch (blockType) {
            case TEXT, H1, H2: {
                postBlock.setTextBlock(
                        TextBlock.builder()
                                .content(postBlockReq.textBlockReq().content())
                                .textStyleType(postBlockReq.textBlockReq().textStyle())
                                .build()
                );
            }break;

            case IMAGE: {

                String imageKey = s3Service.upload(postBlockReq.imageBlockReq().image(), "posts");

                if(imageKey == null) {
                    return;
                }

                postBlock.setImageBlock(
                        ImageBlock.builder()
                                .imageKey(imageKey)
                                .imageName(postBlockReq.imageBlockReq().image().getOriginalFilename())
                                .width(postBlockReq.imageBlockReq().width())
                                .height(postBlockReq.imageBlockReq().height())
                                .build()
                );
            }break;

            case LIST: {

                AtomicInteger count = new AtomicInteger(1);

                postBlock.setListBlock(
                        ListBlock.builder()
                                .listBlock_type(postBlockReq.listBlockReq().listType())
                                .listItems(
                                        postBlockReq.listBlockReq().contents().stream()
                                                .map(textBlockReq ->
                                                        ListItem.builder().
                                                                content(textBlockReq.content())
                                                                .textStyleType(textBlockReq.textStyle())
                                                                .itemOrder(count.getAndIncrement())
                                                                .build()
                                                ).toList()
                                )
                                .build()
                );

            }break;
        }

    }


    @Transactional(readOnly = true)
    List<PostBlockRes> getContentsByPost(Post post) {

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

                    String imageUrl = s3Service.generateGetUrl(postBlock.getImageBlock().getImageKey());

                    postBlockRes.setImageBlockRes(
                            ImageBlockRes.builder()
                                    .imageUrl(imageUrl)
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


    @Transactional
    // 통합 검색 (N+1 해결 버전)
    public Page<PostReadRes> search(String keyword, Pageable pageable) {
        // [Step 1] 조건에 맞는 ID들만 페이징해서 가져옴 (매우 빠름)
        Page<Long> postIdPage = postRepository.findIdsByIntegratedSearch(keyword, pageable);

        return convertToDtoPage(postIdPage, pageable);
    }

    @Transactional
    // 전체 조회 (N+1 해결 버전)
    public Page<PostReadRes> readAllPosts(Pageable pageable) {
        // [Step 1] ID들만 페이징 조회
        Page<Long> postIdPage = postRepository.findAllIds(pageable);

        return convertToDtoPage(postIdPage, pageable);
    }

    // 공통 변환 로직 (2단계 조회)
    private Page<PostReadRes> convertToDtoPage(Page<Long> postIdPage, Pageable pageable) {
        if (postIdPage.isEmpty()) {
            return Page.empty(pageable);
        }

        // [Step 2] 가져온 ID들로 상세 데이터(User, Tag)를 JOIN FETCH로 한 번에 조회
        List<Post> posts = postRepository.findAllByIdsWithDetails(postIdPage.getContent());
        java.util.Map<Long, Post> postMap = posts.stream()
                .collect(java.util.stream.Collectors.toMap(Post::getId, p -> p));

        // [Step 3] DTO 변환 (이미 메모리에 데이터가 다 있어서 쿼리 안나감)
        List<PostReadRes> dto = postIdPage.getContent().stream()
                .map(postMap::get)
                .map(post -> {
                    return PostReadRes.from(post, getContentsByPost(post));
                })
                .toList();

        return new PageImpl<>(dto, pageable, postIdPage.getTotalElements());
    }
    @Transactional
    public PostReadRes readPost(Long postId){
        Post post = postRepository.findByIdWithDetails(postId)
                .orElseThrow(() -> new PostNotFoundException("해당 자료를 찾을 수 없습니댜."));

        postRepository.updateViews(postId);
        return PostReadRes.from(post, getContentsByPost(post));
    }

    @Transactional(readOnly = true)
    public Page<PostReadRes> readPopularPosts(Pageable pageable){
        return postRepository.findPopularPosts(pageable)
                .map(post -> {
                    return PostReadRes.from(post, getContentsByPost(post));
                });
    }
    @Transactional(readOnly = true)
    public Page<PostReadRes> readAllViewedPosts(Pageable pageable, Long user_id){
        return postViewRepository.findAllViewedPostsByUserId(pageable ,user_id)
                .map(post -> {
                    return PostReadRes.from(post, getContentsByPost(post));
                });
    }
    @Transactional(readOnly = true)
    public Page<PostReadRes> readAllViewedPostsSortRecentViewed(Pageable pageable, Long user_id){
        return postViewRepository.findAllPostsByUserIdSortRecentViewed(pageable, user_id)
                .map(post -> {
                    return PostReadRes.from(post, getContentsByPost(post));
                });
    }
    @Transactional(readOnly = true)
    public Page<PostReadRes> readAllWrittenPosts(Pageable pageable, Long user_id){
        return postRepository.findPostsByUserId(user_id, pageable)
                .map(post -> {
                    return PostReadRes.from(post, getContentsByPost(post));
                });
    }
    @Transactional(readOnly = true)
    public Page<PostReadRes> readAllLikedPosts(Pageable pageable, Long user_id){
        return postLikeRepository.findLikedPostsByUserId(user_id, pageable)
                .map(post -> {
                    return PostReadRes.from(post, getContentsByPost(post));
                });
    }

    @Transactional(readOnly = true)
    public Page<PostReadRes> readAllPostsInDuration(Instant from, Instant to, Pageable pageable){
        return postRepository.findAllPostsByCreateAtInDuration(from, to, pageable)
                .map(post -> {
                    return PostReadRes.from(post, getContentsByPost(post));
                });
    }
}
