package org.example.chain.domain.post.data.req;

import org.example.chain.domain.post.enums.ListType;

import java.util.List;

public record ListBlockReq (ListType listType, List<TextBlockReq> contents) {}
