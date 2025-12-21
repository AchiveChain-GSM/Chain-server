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
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"list_block_id", "item_order"})})
public class ListItem {

    @Id
    @GeneratedValue
    @Column(name = "list_item_id")
    private Long listItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "list_block_id")
    private ListBlock listBlock;

    @Enumerated(EnumType.STRING)
    @Column(name = "text_style_type")
    private TextStyleType  textStyleType;

    @Column(name = "content")
    private String content;

    @Column(name = "item_order")
    private int itemOrder;

}