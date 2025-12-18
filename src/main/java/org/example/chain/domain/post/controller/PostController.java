package org.example.chain.domain.post.controller;

import lombok.RequiredArgsConstructor;
import org.example.chain.domain.post.data.req.PostCreateReq;
import org.example.chain.domain.post.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/post")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping("/create")
    public ResponseEntity<HttpStatus> createPost(@RequestBody PostCreateReq request){
        return postService.createPost(request);
    }
}
