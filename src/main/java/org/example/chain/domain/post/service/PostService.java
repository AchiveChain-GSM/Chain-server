package org.example.chain.domain.post.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.chain.domain.post.data.req.PostCreateReq;
import org.example.chain.domain.post.data.res.PostReadRes;
import org.example.chain.domain.post.entity.Post;
import org.example.chain.domain.post.repository.PostRepository;
import org.example.chain.global.error.exception.PostNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;

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
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("해당 자료를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        return PostReadRes.builder()
                .title(post.getTitle())
                .content(post.getContent())
                .likes(post.getLikes())
                .comments(post.getComments())
                .tags(post.getTags())
                .build();
    }

    @Transactional
    public Page<PostReadRes> readAllPosts(Pageable pageable){
        return postRepository.findAll(pageable)
                .map(PostReadRes::from);
    }
    @Transactional
    public Page<PostReadRes> readPopularPosts(Pageable pageable){
        return postRepository.findPopularPost(pageable)
                .map(PostReadRes::from);
    }
}
