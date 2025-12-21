package org.example.chain.domain.post.data.res;

import lombok.Builder;
import org.example.chain.domain.post.enums.TextStyleType;

@Builder
public record TextBlockRes (String content, TextStyleType textStyle) {}
