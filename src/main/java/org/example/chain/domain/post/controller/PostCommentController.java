package org.example.chain.domain.post.controller;

import lombok.RequiredArgsConstructor;
import org.example.chain.domain.post.data.req.PostCommentCreateReq;
import org.example.chain.domain.post.data.res.PostCommentReadRes;
import org.example.chain.domain.post.service.PostCommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
public class PostCommentController {
    private final PostCommentService commentService;

    @PostMapping
    public ResponseEntity<Void> createComment(
            @PathVariable Long postId,
            @RequestBody PostCommentCreateReq request) {

        commentService.createComment(postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
