package org.example.chain.domain.post.data.res;

import lombok.Builder;

@Builder
public record ImageBlockRes (String imageUrl, int width, int height) {}
