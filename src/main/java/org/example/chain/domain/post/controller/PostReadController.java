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

import java.util.List;

@RestController
@RequestMapping("/api/post/read")
@RequiredArgsConstructor
public class PostReadController {
    private final PostService postService;

    @GetMapping("/id/{postId}")
    public PostReadRes readPost(@PathVariable Long postId){
        return postService.readPost(postId);
    }
    @GetMapping("/recent")
    public ResponseEntity<Page<PostReadRes>> readPosts(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable){
        Page<PostReadRes> posts = postService.readAllPosts(pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/search")
    public Page<PostReadRes> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<String> tags,
            Pageable pageable) {
        return postService.search(keyword, tags, pageable);
    }
}
