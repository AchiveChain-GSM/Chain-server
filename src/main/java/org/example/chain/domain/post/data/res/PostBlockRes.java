package org.example.chain.domain.post.data.res;

import lombok.Builder;
import lombok.Setter;
import org.example.chain.domain.post.enums.BlockType;

@Builder
@Setter
public class PostBlockRes {
    BlockType blockType;
    TextBlockRes textBlock;
    ListBlockRes listBlock;
    Long sortOrder;
}
