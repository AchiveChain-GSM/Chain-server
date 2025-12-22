package org.example.chain.domain.post.data.req;

import org.example.chain.domain.post.enums.TextStyleType;

public record TextBlockReq (String content, TextStyleType textStyle) {}
