package org.example.chain.domain.post.controller;

import lombok.RequiredArgsConstructor;
import org.example.chain.domain.post.data.req.PostCreateReq;
import org.example.chain.domain.post.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping("/create")
    public ResponseEntity<Void> createPost(@RequestBody PostCreateReq request){
        Long postId = postService.createPost(request);
        return ResponseEntity.created(java.net.URI.create("/api/posts/" + postId)).build();
    }
}
