package org.example.chain.domain.post.data.req;

import java.time.Instant;

public record TimelinePostReq (Instant from, Instant to){}
