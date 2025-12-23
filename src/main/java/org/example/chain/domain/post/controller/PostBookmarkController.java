package org.example.chain.domain.post.controller;

import lombok.RequiredArgsConstructor;
import org.example.chain.domain.post.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts/{postId}/bookmark")
@RequiredArgsConstructor
public class PostBookmarkController {

    private final PostService postService;

    //게시물에 북마크 적용 페이지
    @PostMapping
    public ResponseEntity<Void> toggleBookmark(@PathVariable Long postId) {
        postService.toggleBookmark(postId);
        return ResponseEntity.ok().build();
    }
}