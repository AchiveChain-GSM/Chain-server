package org.example.chain.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.example.chain.domain.post.data.req.PostCreateReq;
import org.example.chain.domain.post.data.res.PostReadRes;
import org.example.chain.domain.post.entity.Post;
import org.example.chain.domain.post.repository.PostLikeRepository;
import org.example.chain.domain.post.repository.PostRepository;
import org.example.chain.domain.post.repository.PostViewRepository;
import org.example.chain.global.error.exception.PostNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

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
        postRepository.save(post);
        return new ResponseEntity<>(HttpStatus.CREATED);
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
        return postRepository.findAllPostsByCreatedAtInDuration(from, to, pageable)
                .map(PostReadRes::from);
    }


}
