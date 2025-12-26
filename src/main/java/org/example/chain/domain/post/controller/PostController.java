package org.example.chain.domain.post.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.chain.domain.post.data.req.PostCreateReq;
import org.example.chain.domain.post.data.req.PostReportReq;
import org.example.chain.domain.post.data.req.PostUpdateReq;
import org.example.chain.domain.post.data.res.PostReportRes;
import org.example.chain.domain.post.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    //게시물 생성 페이지, 바로 조회할 수 있는 URL 반환
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createPost(@ModelAttribute PostCreateReq request){
        log.info("자료 생성");
        Long postId = postService.createPost(request);
        log.info("자료 생성 완료");

        String url = "/api/posts/" + postId;

        return ResponseEntity
                .created(java.net.URI.create(url))
                .body(url);
    }

    //게시물 수정 페이지
    @PostMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updatePost(
            @ModelAttribute PostUpdateReq updateRequest // @RequestBody -> @ModelAttribute
    ){
        log.info("자료 수정");
        postService.updatePost(updateRequest);
        log.info("자료 수정 완료");
        return ResponseEntity.noContent().build();
    }

    //게시물 삭제 페이지
    @DeleteMapping("/delete/{post_id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long post_id
    ){
        log.info("자료 삭제");
        postService.deletePost(post_id);
        log.info("자료 삭제 완료");
        return ResponseEntity.ok().build();
    }

    //신고 접수 생성 페이지
    @PostMapping("/report/{postId}")
    public ResponseEntity<Void> createReportPost (
            @PathVariable Long postId,
            @RequestBody PostReportReq report
            ) {
        postService.createReport(report, postId);
        return ResponseEntity.ok().build();
    }

    //사용자, 신고 접수 목록 조회 페이지
    @GetMapping("/reportRead")
    public ResponseEntity<Page<PostReportRes>> readReportPost (
            @PageableDefault(size = 20, sort = "createAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<PostReportRes> postReports = postService.readReport(pageable);
        return ResponseEntity.ok(postReports);
    }

    //사용자 전용, 신고 접수 삭제 페이지
    @DeleteMapping("/reportDelete/{post_id}")
    public ResponseEntity<Void> deleteReportPost (
            @PathVariable Long post_id
    ){
        postService.deleteReport(post_id);
        return ResponseEntity.ok().build();
    }


}
