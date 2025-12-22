package org.example.chain.domain.post.data.req;

import org.springframework.web.multipart.MultipartFile;

public record ImageBlockReq (MultipartFile image, int width, int height) {}
