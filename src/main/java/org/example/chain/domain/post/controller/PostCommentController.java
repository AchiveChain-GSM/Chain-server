package org.example.chain.domain.post.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.chain.domain.post.data.req.PostCommentCreateReq;
import org.example.chain.domain.post.data.res.PostCommentReadRes;
import org.example.chain.domain.post.service.PostCommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
public class PostCommentController {
    private final PostCommentService commentService;

    //게시물에, 댓글 생성 페이지
    @PostMapping
    public ResponseEntity<String> createComment(
            @PathVariable Long postId,
            @RequestBody PostCommentCreateReq request) {
        log.info("댓글 작성");

        String url = "/api/posts/" + postId + "comments";

        commentService.createComment(postId, request);
        log.info("댓글 작성 완료");
        return ResponseEntity
                .created(java.net.URI.create(url))
                .body(url);
    }

    @GetMapping
    public ResponseEntity<List<PostCommentReadRes>> readComments(
            @PathVariable Long postId
    ) {
        List<PostCommentReadRes> comments = commentService.readComments(postId);
        return ResponseEntity.ok(comments);
    }
}
