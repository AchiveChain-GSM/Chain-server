package org.example.chain.domain.post.data.res;

import lombok.Builder;

@Builder
public record PostReportRes (String description, Long post_id) {}
