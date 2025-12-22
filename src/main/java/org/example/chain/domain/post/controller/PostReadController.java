package org.example.chain.domain.post.controller;

import lombok.RequiredArgsConstructor;
import org.example.chain.domain.post.data.req.TimelinePostReq;
import org.example.chain.domain.post.data.res.PostReadRes;
import org.example.chain.domain.post.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostReadController {
    private final PostService postService;

    @GetMapping("/{postId}")
    public ResponseEntity<PostReadRes> readPost(@PathVariable Long postId){
        return ResponseEntity.ok(postService.readPost(postId));
    }
    @GetMapping("/recent")
    public ResponseEntity<Page<PostReadRes>> readRecentPosts(
            @PageableDefault(size = 20, sort = "createAt", direction = Sort.Direction.DESC)
            Pageable pageable){
        Page<PostReadRes> posts = postService.readAllPosts(pageable);
        return ResponseEntity.ok(posts);
    }
    @GetMapping("/popular")
    public ResponseEntity<Page<PostReadRes>> readPopularPosts(@PageableDefault(size = 20) Pageable pageable) {
        Page<PostReadRes> posts = postService.readPopularPosts(pageable);
        return ResponseEntity.ok(posts);
    }
    @GetMapping("/most-view")
    public ResponseEntity<Page<PostReadRes>> readMostViewPosts(
            @PageableDefault(size = 20, sort = "views", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<PostReadRes> posts = postService.readAllPosts(pageable);
        return ResponseEntity.ok(posts);
    }

    // 검색 엔진
    @GetMapping("/search")
    public ResponseEntity<Page<PostReadRes>> search(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        return ResponseEntity.ok(postService.search(keyword, pageable));
    }

    // 최근 본 자료
    @GetMapping("/viewed/{userId}")
    public ResponseEntity<Page<PostReadRes>> readUserViewedPosts(
            @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "createAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ){
        Page<PostReadRes> posts = postService.readAllViewedPosts(pageable, userId);
        return  ResponseEntity.ok(posts);
    }
    @GetMapping("/viewed/{userId}/recent")
    public ResponseEntity<Page<PostReadRes>> readUserViewedPostsSortByRecentView(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable
    ){
        Page<PostReadRes> posts = postService.readAllViewedPostsSortRecentViewed(pageable, userId);
        return  ResponseEntity.ok(posts);
    }
    @GetMapping("/viewed/{userId}/likes")
    public ResponseEntity<Page<PostReadRes>> readUserViewedPostsSortByLikes(
            @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "likes", direction = Sort.Direction.DESC) Pageable pageable
    ){
        Page<PostReadRes> posts = postService.readAllViewedPosts(pageable, userId);
        return  ResponseEntity.ok(posts);
    }
    @GetMapping("/viewed/{userId}/views")
    public ResponseEntity<Page<PostReadRes>> readUserViewedPostsByViews(
            @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "views", direction = Sort.Direction.DESC) Pageable pageable
    ){
        Page<PostReadRes> posts = postService.readAllViewedPosts(pageable, userId);
        return  ResponseEntity.ok(posts);
    }

    // 내가 작성한 자료
    @GetMapping("/written/{userId}")
    public ResponseEntity<Page<PostReadRes>> readUserWrittenPosts(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable
    ){
        Page<PostReadRes> posts = postService.readAllWrittenPosts(pageable, userId);
        return ResponseEntity.ok(posts);
    }
    @GetMapping("/written/{userId}/recent")
    public ResponseEntity<Page<PostReadRes>> readUserWrittenPostsSortByRecentRitten(
            @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "createAt", direction = Sort.Direction.DESC) Pageable pageable
    ){
        Page<PostReadRes> posts = postService.readAllWrittenPosts(pageable, userId);
        return ResponseEntity.ok(posts);
    }
    @GetMapping("/written/{userId}/likes")
    public ResponseEntity<Page<PostReadRes>> readUserWrittenPostsSortByLikes(
            @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "likes", direction = Sort.Direction.DESC) Pageable pageable
    ){
        Page<PostReadRes> posts = postService.readAllWrittenPosts(pageable, userId);
        return ResponseEntity.ok(posts);
    }
    @GetMapping("/written/{userId}/views")
    public ResponseEntity<Page<PostReadRes>> readUserWrittenPostsSortByViews(
            @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "views", direction = Sort.Direction.DESC) Pageable pageable
    ){
        Page<PostReadRes> posts = postService.readAllWrittenPosts(pageable, userId);
        return ResponseEntity.ok(posts);
    }

    // 내가 좋아요 누른 자요
    @GetMapping("/liked/{userId}")
    public ResponseEntity<Page<PostReadRes>> readUserLikedPosts(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<PostReadRes> posts = postService.readAllLikedPosts(pageable, userId);
        return ResponseEntity.ok(posts);
    }

    // 타임라인
    @PostMapping("/timeline")
    public ResponseEntity<Page<PostReadRes>> readTimelinePosts(
            @RequestBody TimelinePostReq timelinePostReq, Pageable pageable) {
        Page<PostReadRes> posts = postService.readAllPostsInDuration(timelinePostReq.from(), timelinePostReq.to(), pageable);
        return ResponseEntity.ok(posts);
    }
}
