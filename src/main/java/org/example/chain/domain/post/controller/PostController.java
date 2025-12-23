package org.example.chain.domain.post.controller;

import lombok.RequiredArgsConstructor;
import org.example.chain.domain.post.data.req.PostCreateReq;
import org.example.chain.domain.post.data.req.PostReportReq;
import org.example.chain.domain.post.data.req.PostUpdateReq;
import org.example.chain.domain.post.data.res.PostReportRes;
import org.example.chain.domain.post.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    //게시물 생성 페이지, 바로 조회할 수 있는 URL 반환
    @PostMapping("/create/{user_id}")
    public ResponseEntity<Void> createPost(
            @PathVariable Long user_id,
            @RequestBody PostCreateReq request){
        Long postId = postService.createPost(request, user_id);
        return ResponseEntity.created(java.net.URI.create("/api/posts/" + postId)).build();
    }

    //게시물 수정 페이지
    @PostMapping("/update/{user_id}")
    public ResponseEntity<Void> updatePost(
        @PathVariable Long user_id,
        @RequestBody PostUpdateReq updateRequest
    ){
        postService.updatePost(updateRequest, user_id);
        return ResponseEntity.noContent().build();
    }

    //게시물 삭제 페이지
    @GetMapping("/delete/{post_id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long post_id
    ){
        postService.deletePost(post_id);
        return ResponseEntity.ok().build();
    }

    //신고 접수 생성 페이지
    @PostMapping("/reportCreate/{user_id}")
    public ResponseEntity<Void> createReportPost (
            @PathVariable Long user_id,
            @RequestBody PostReportReq report
            ) {
        postService.createReport(report, user_id);
        return ResponseEntity.ok().build();
    }

    //사용자, 신고 접수 목록 조회 페이지
    @GetMapping("/reportRead/{user_id}")
    public ResponseEntity<Page<PostReportRes>> readReportPost (
            @PathVariable Long user_id,
            @PageableDefault(size = 20, sort = "createAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<PostReportRes> postReports = postService.readReport(pageable, user_id);

        return ResponseEntity.ok(postReports);
    }

    //사용자 전용, 신고 접수 삭제 페이지
    @GetMapping("/reportDelete/{post_id}")
    public ResponseEntity<Void> deleteReportPost (
            @PathVariable Long post_id
    ){
        postService.deleteReport(post_id);
        return ResponseEntity.ok().build();
    }


}
