package org.example.chain.domain.post.controller;

import lombok.RequiredArgsConstructor;
import org.example.chain.domain.post.data.res.PostReadRes;
import org.example.chain.domain.post.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/post/read")
@RequiredArgsConstructor
public class PostReadController {
    private final PostService postService;

    @GetMapping("/id/{postId}")
    public PostReadRes readPost(@PathVariable Long postId){
        return postService.readPost(postId);
    }
    @GetMapping("/recent/posts")
    public ResponseEntity<Page<PostReadRes>> readPosts( // 이건 이름 바꿀어ㅑ 잘못만듬
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable){
        Page<PostReadRes> posts = postService.readAllPosts(pageable);
        return ResponseEntity.ok(posts);
    }
    @GetMapping("/popular/posts")  //매 일 단위로 10의 가장 좋아요가 많은 게시물을 표시
    public ResponseEntity<Page<PostReadRes>> readPopularPosts(@PageableDefault(size = 20) Pageable pageable) {
        Page<PostReadRes> posts = postService.readPopularPosts(pageable);
        return ResponseEntity.ok(posts);
    }
    @GetMapping("/most-view/posts")
    public ResponseEntity<Page<PostReadRes>> readMostViewPosts(
            @PageableDefault(size = 20, sort = "views", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<PostReadRes> posts = postService.readAllPosts(pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("user-viewed-posts/{user_id}")
    public ResponseEntity<Page<PostReadRes>> readUserViewedPosts(
            @PathVariable Long user_id,
            @PageableDefault(size = 20) Pageable pageable
    ){
        Page<PostReadRes> posts = postService.readAllViewedPosts(pageable, user_id);
        return  ResponseEntity.ok(posts);
    }

    @GetMapping("user-written-posts/{user_id}")
    public ResponseEntity<Page<PostReadRes>> readUserWrittenPosts(
            @PathVariable Long user_id,
            @PageableDefault(size = 20) Pageable pageable
    ){
        Page<PostReadRes> posts = postService.readAllWrittenPosts(pageable, user_id);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("user-liked-posts/{user_id}")
    public ResponseEntity<Page<PostReadRes>> readUserLikedPosts(
            @PathVariable Long user_id,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<PostReadRes> posts = postService.readAllLikedPosts(pageable, user_id);
        return ResponseEntity.ok(posts);
    }

}
