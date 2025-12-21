package org.example.chain.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.example.chain.domain.post.data.req.PostBlockReq;
import org.example.chain.domain.post.data.req.PostCreateReq;
import org.example.chain.domain.post.data.res.PostReadRes;
import org.example.chain.domain.post.entity.*;
import org.example.chain.domain.post.enums.BlockType;
import org.example.chain.domain.post.repository.PostLikeRepository;
import org.example.chain.domain.post.repository.PostRepository;
import org.example.chain.domain.post.repository.PostViewRepository;
import org.example.chain.global.error.exception.PostNotFoundException;
import org.example.chain.global.util.ImageUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final PostViewRepository postViewRepository;
    private final PostLikeRepository postLikeRepository;

    @Transactional
    public ResponseEntity<HttpStatus> createPost(PostCreateReq request){
        Post post = Post.builder()
                .title(request.title())
                .content(request.content())
                .tags(request.tags())
                .build();

        int count = 1;
        for(var postBlockReg : request.blocks()) {

            PostBlock postBlock = PostBlock.builder()
                    .post(post)
                    .blockType(postBlockReg.blockType())
                    .sortOrder(count++)
                    .build();

            createBlockByType(postBlockReg.blockType(), postBlock, postBlockReg);

        }

        postRepository.save(post);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    void createBlockByType(BlockType blockType, PostBlock postBlock, PostBlockReq  postBlockReq) {

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

                String imageUrl = saveImage(postBlockReq.imageBlockReq().image());

                if(imageUrl == null) {
                    return;
                }

                postBlock.setImageBlock(
                        ImageBlock.builder()
                                .imageUrl(imageUrl)
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

    String saveImage (MultipartFile file) {
        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            byte[] bytes = file.getBytes();
            Files.write(Paths.get(ImageUtil.imageDirPath, fileName), bytes);

            return "/images/" + fileName;

        }catch (Exception e){
            throw new RuntimeException("Failed to save image", e);
        }
    }




    @Transactional
    public PostReadRes readPost(Long postId){
        Post post = postRepository.findPostByPostId(postId)
                .orElseThrow(() -> new PostNotFoundException("해당 자료를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        post.addViews();
        return PostReadRes.builder()
                .title(post.getTitle())
                .content(post.getContent())
                .likes(post.getLikes())
                .comments(post.getComments())
                .tags(post.getTags())
                .build();
    }

    @Transactional(readOnly = true)
    public Page<PostReadRes> readAllPosts(Pageable pageable){
        return postRepository.findAll(pageable)
                .map(PostReadRes::from);
    }
    @Transactional(readOnly = true)
    public Page<PostReadRes> readPopularPosts(Pageable pageable){
        return postRepository.findPopularPosts(pageable)
                .map(PostReadRes::from);
    }
    @Transactional(readOnly = true)
    public Page<PostReadRes> readAllViewedPosts(Pageable pageable, Long user_id){
        return postViewRepository.findAllViewedPostsByUserId(pageable ,user_id)
                .map(PostReadRes::from);
    }
    @Transactional(readOnly = true)
    public Page<PostReadRes> readAllViewedPostsSortRecentViewed(Pageable pageable, Long user_id){
        return postViewRepository.findAllPostsByUserIdSortRecentViewed(pageable, user_id).map(PostReadRes::from);
    }
    @Transactional(readOnly = true)
    public Page<PostReadRes> readAllWrittenPosts(Pageable pageable, Long user_id){
        return postRepository.findPostsByUserId(user_id, pageable).map(PostReadRes::from);
    }
    @Transactional(readOnly = true)
    public Page<PostReadRes> readAllLikedPosts(Pageable pageable, Long user_id){
        return postLikeRepository.findLikedPostsByUserId(user_id, pageable)
                .map(PostReadRes::from);
    }

    @Transactional(readOnly = true)
    public Page<PostReadRes> readAllPostsInDuration(Instant from, Instant to, Pageable pageable){
        return postRepository.findAllPostsByCreateAtInDuration(from, to, pageable)
                .map(PostReadRes::from);
    }


}
