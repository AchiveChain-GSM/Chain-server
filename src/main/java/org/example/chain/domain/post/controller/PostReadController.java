package org.example.chain.domain.post.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.chain.domain.post.data.req.TimelinePostReq;
import org.example.chain.domain.post.data.res.PostDetailReadRes;
import org.example.chain.domain.post.data.res.PostReadRes;
import org.example.chain.domain.post.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostReadController {
    private final PostService postService;

    //게시물 상세 조회 페이지
    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailReadRes> readPost(@PathVariable Long postId){
        log.info("자료 상세 조회");
        return ResponseEntity.ok(postService.readPost(postId));
    }
    //가장 최근 게시물 조회 페이지
    @GetMapping("/recent")
    public ResponseEntity<Page<PostReadRes>> readRecentPosts(
            @PageableDefault(size = 20, sort = "createAt", direction = Sort.Direction.DESC)
            Pageable pageable){
        log.info("최근 자료 조회");
        Page<PostReadRes> posts = postService.readAllPosts(pageable);
        log.info("최근 자료 조회 완료");
        return ResponseEntity.ok(posts);
    }
    //가장 좋아요가 많은 게시물 조회 페이지
    @GetMapping("/popular")
    public ResponseEntity<Page<PostReadRes>> readPopularPosts(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("좋아요순 자료 조회");
        Page<PostReadRes> posts = postService.readPopularPosts(pageable);
        log.info("좋아요순 자료 조회 완료");
        return ResponseEntity.ok(posts);
    }
    //가장 많이 읽힌 게시물 조회 페이지
    @GetMapping("/most-view")
    public ResponseEntity<Page<PostReadRes>> readMostViewPosts(
            @PageableDefault(size = 20, sort = "views", direction = Sort.Direction.DESC)
            Pageable pageable) {
        log.info("조회순 자료 조회");
        Page<PostReadRes> posts = postService.readAllPosts(pageable);
        log.info("조회순 자료 조회 완료");
        return ResponseEntity.ok(posts);
    }

    // 검색 엔진
    @GetMapping("/search")
    public ResponseEntity<Page<PostReadRes>> search(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        log.info("검색 엔진");
        return ResponseEntity.ok(postService.search(keyword, pageable));
    }

    //가장 최근에 읽은 게시물 조회 페이지
    @GetMapping("/viewed")
    public ResponseEntity<Page<PostReadRes>> readUserViewedPosts(
            @PageableDefault(size = 20, sort = "createAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ){
        log.info("최근 읽은 자료");
        Page<PostReadRes> posts = postService.readAllViewedPosts(pageable);
        log.info("최근 읽은 자료 완료");
        return  ResponseEntity.ok(posts);
    }
    //가장 최근에 생성된, 읽은 게시물 조회 페이지
    @GetMapping("/viewed/recent")
    public ResponseEntity<Page<PostReadRes>> readUserViewedPostsSortByRecentView(
            @PageableDefault(size = 20) Pageable pageable
    ){
        log.info("최근 읽은 자료 최신순");
        Page<PostReadRes> posts = postService.readAllViewedPostsSortRecentViewed(pageable);
        log.info("최근 읽은 자료 최신순 완료");
        return  ResponseEntity.ok(posts);
    }
    //가장 많은 조회수, 읽은 게시물 조회 페이지
    @GetMapping("/viewed/likes")
    public ResponseEntity<Page<PostReadRes>> readUserViewedPostsSortByLikes(
            @PageableDefault(size = 20, sort = "likes", direction = Sort.Direction.DESC) Pageable pageable
    ){
        log.info("최근 읽은 자료 좋아요순");
        Page<PostReadRes> posts = postService.readAllViewedPosts(pageable);
        log.info("최근 읽은 자료 좋아요순 완료");
        return  ResponseEntity.ok(posts);
    }
    //가장 많이 읽힌, 그리고 해당 사용자가 읽은 게시물 조회 페이지
    @GetMapping("/viewed/views")
    public ResponseEntity<Page<PostReadRes>> readUserViewedPostsByViews(
            @PageableDefault(size = 20, sort = "views", direction = Sort.Direction.DESC) Pageable pageable
    ){
        log.info("최근 읽은 자료 조회순");
        Page<PostReadRes> posts = postService.readAllViewedPosts(pageable);
        log.info("최근 읽은 자료 조회순 완료");
        return  ResponseEntity.ok(posts);
    }

    //작성한 게시물 조회 페이지
    @GetMapping("/written")
    public ResponseEntity<Page<PostReadRes>> readUserWrittenPosts(
            @PageableDefault(size = 20) Pageable pageable
    ){
        log.info("내가 쓴 자료");
        Page<PostReadRes> posts = postService.readAllWrittenPosts(pageable);
        log.info("내가 쓴 자료 완료");
        return ResponseEntity.ok(posts);
    }
    //가장 최근에 작성한 게시물 조회 페이지
    @GetMapping("/written/recent")
    public ResponseEntity<Page<PostReadRes>> readUserWrittenPostsSortByRecentRitten(
            @PageableDefault(size = 20, sort = "createAt", direction = Sort.Direction.DESC) Pageable pageable
    ){
        log.info("내가 쓴 자료 최신순");
        Page<PostReadRes> posts = postService.readAllWrittenPosts(pageable);
        log.info("내가 쓴 자료 최신순 완료");
        return ResponseEntity.ok(posts);
    }
    //가장 좋아요를 받은, 작성한 게시물 조회 페이지
    @GetMapping("/written/likes")
    public ResponseEntity<Page<PostReadRes>> readUserWrittenPostsSortByLikes(
            @PageableDefault(size = 20, sort = "likes", direction = Sort.Direction.DESC) Pageable pageable
    ){
        log.info("내가 쓴 자료 좋아요순");
        Page<PostReadRes> posts = postService.readAllWrittenPosts(pageable);
        log.info("내가 쓴 자료 좋아요순 완료");
        return ResponseEntity.ok(posts);
    }
    //가장 많이 읽힌, 작성한 게시물 조회 페이지
    @GetMapping("/written/views")
    public ResponseEntity<Page<PostReadRes>> readUserWrittenPostsSortByViews(
            @PageableDefault(size = 20, sort = "views", direction = Sort.Direction.DESC) Pageable pageable
    ){
        log.info("내가 쓴 자료 조회순");
        Page<PostReadRes> posts = postService.readAllWrittenPosts(pageable);
        log.info("내가 쓴 자료 조회순 완료");
        return ResponseEntity.ok(posts);
    }

    //사용자가 좋아요를 누른 게시물 조회 페이지
    @GetMapping("/liked")
    public ResponseEntity<Page<PostReadRes>> readUserLikedPosts(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        log.info("좋아요 누른 자료");
        Page<PostReadRes> posts = postService.readAllLikedPosts(pageable);
        log.info("좋아요 누른 자료 완료");
        return ResponseEntity.ok(posts);
    }

    //사용자가, 즐겨찾기 한 게시물 조회 페이지
    @GetMapping("/bookmarked")
    public ResponseEntity<Page<PostReadRes>> readUserBookmarkedPosts(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        log.info("즐겨찾기 한 자료");
        Page<PostReadRes> posts = postService.readAllBookMarkedPosts(pageable);
        log.info("즐겨찾기 한 자료 완료");
        return ResponseEntity.ok(posts);
    }

    //사용자가, 즐겨 찾기 한 가장 최근의 게시물 조회 페이지
    @GetMapping("/bookmarked/recent")
    public ResponseEntity<Page<PostReadRes>> readUserBookmarkedPostsByRecent(
            @PageableDefault(size = 20, sort = "createAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("최근에 즐겨찾기 한 자료");
        Page<PostReadRes> posts = postService.readAllBookMarkedPosts(pageable);
        log.info("최근에 즐겨찾기 한 자료 완료");
        return ResponseEntity.ok(posts);
    }

    //사용자가, 즐겨 찾기 한 가장 좋아요가 많은 게시물 조회 페이지
    @GetMapping("/bookmarked/likes")
    public ResponseEntity<Page<PostReadRes>> readUserBookmarkedPostsByLikes(
            @PageableDefault(size = 20, sort = "likes", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("즐겨찾기 한 자료 좋아요순");
        Page<PostReadRes> posts = postService.readAllBookMarkedPosts(pageable);
        log.info("즐겨찾기 한 자료 좋아요순 완료");
        return ResponseEntity.ok(posts);
    }
    //사용자가, 즐겨 찾기 한 가장 많이 읽힌 게시물 조회 페이지
    @GetMapping("/bookmarked/views")
    public ResponseEntity<Page<PostReadRes>> readUserBookmarkedPostsByViews(
            @PageableDefault(size = 20, sort = "views", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("즐겨찾기 한 자료 조회순");
        Page<PostReadRes> posts = postService.readAllBookMarkedPosts(pageable);
        log.info("즐겨찾기 한 자료 조회순 완료");
        return ResponseEntity.ok(posts);
    }

    //지정한 기간 동안 생성된 게시물 조회 페이지
    @PostMapping("/timeline")
    public ResponseEntity<Page<PostReadRes>> readTimelinePosts(
            @RequestBody TimelinePostReq timelinePostReq,
            @PageableDefault(size = 100, sort = "createAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("타임라인 조회 - 페이지 번호: {}", pageable.getPageNumber());
        Page<PostReadRes> posts = postService.readAllPostsInDuration(timelinePostReq.from(), timelinePostReq.to(), pageable);

        log.info("타임라인 완료");
        return ResponseEntity.ok(posts);
    }
}
