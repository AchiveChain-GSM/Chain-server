package org.example.chain.domain.post.data.req;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public record PostUpdateReq (Long post_id, String title, String content, List<String> tags, List<MultipartFile> images, List<Long> removeImage_ids) {}
