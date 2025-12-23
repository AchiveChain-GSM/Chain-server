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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> createPost(@ModelAttribute PostCreateReq request){
        Long postId = postService.createPost(request);
        return ResponseEntity.created(java.net.URI.create("/api/posts/" + postId)).build();
    }

    @PostMapping("/update/{user_id}")
    public ResponseEntity<Void> updatePost(
        @PathVariable Long user_id,
        @RequestBody PostUpdateReq updateRequest
    ){
        postService.updatePost(updateRequest, user_id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/delete/{post_id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long post_id
    ){
        postService.deletePost(post_id);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/reportCreate/{user_id}")
    public ResponseEntity<Void> createReportPost (
            @PathVariable Long user_id,
            @RequestBody PostReportReq report
            ) {
        postService.reportPost(report, user_id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/reportRead/{user_id}")
    public ResponseEntity<Page<PostReportRes>> readReportPost (
            @PathVariable Long user_id,
            @PageableDefault(size = 20, sort = "createAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<PostReportRes> postReports = postService.readReport(pageable, user_id);

        return ResponseEntity.ok(postReports);
    }

    @GetMapping("/reportDelete/{post_id}")
    public ResponseEntity<Void> deleteReportPost (
            @PathVariable Long post_id
    ){
        postService.deleteReport(post_id);
        return ResponseEntity.ok().build();
    }


}
