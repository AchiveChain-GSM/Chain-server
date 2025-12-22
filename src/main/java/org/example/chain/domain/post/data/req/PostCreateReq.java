package org.example.chain.domain.post.data.req;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record PostCreateReq(String title, String content, List<String> tags, List<PostBlockReq> blocks, List<MultipartFile> images) {
}