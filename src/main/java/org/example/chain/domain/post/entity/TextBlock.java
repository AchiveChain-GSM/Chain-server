package org.example.chain.domain.post.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.chain.domain.post.enums.TextStyleType;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TextBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "text_block_id")
    private Long textBlock_id;

    @Column(name = "content")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "text_style_type")
    private TextStyleType textStyleType;


}