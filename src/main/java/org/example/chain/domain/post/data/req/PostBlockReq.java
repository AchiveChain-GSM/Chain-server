package org.example.chain.domain.post.data.req;

import org.example.chain.domain.post.enums.BlockType;

public record PostBlockReq(BlockType blockType, TextBlockReq textBlockReq, ListBlockReq listBlockReq, int sortOrder) {}
