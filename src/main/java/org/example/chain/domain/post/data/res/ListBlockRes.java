package org.example.chain.domain.post.data.res;
import lombok.Builder;
import org.example.chain.domain.post.enums.ListType;

import java.util.List;

@Builder
public record ListBlockRes (ListType listType, List<ListItemRes> contents) {}
