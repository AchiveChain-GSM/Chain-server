package org.example.chain.domain.post.controller;

import lombok.RequiredArgsConstructor;
import org.example.chain.domain.post.data.req.PostCommentCreateReq;
import org.example.chain.domain.post.service.PostCommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
public class PostCommentController {
    private final PostCommentService commentService;

    //게시물에, 댓글 생성 페이지
    @PostMapping
    public ResponseEntity<Void> createComment(
            @PathVariable Long postId,
            @RequestBody PostCommentCreateReq request) {

        commentService.createComment(postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
