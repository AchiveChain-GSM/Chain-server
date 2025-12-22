package org.example.chain.domain.post.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.chain.domain.post.enums.BlockType;

@Entity
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"post_id", "sort_order"})})
public class PostBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_block_id")
    private Long PostBlock_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Enumerated(EnumType.STRING)
    @Column(name = "block_type", nullable = false)
    private BlockType blockType;


    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, optional = true)
    @JoinColumn(name = "text_block_id")
    private TextBlock textBlock;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, optional = true)
    @JoinColumn(name = "list_block_id")
    private ListBlock listBlock;


    @Column(name = "sort_order", nullable = false)
    private Long sortOrder;


}