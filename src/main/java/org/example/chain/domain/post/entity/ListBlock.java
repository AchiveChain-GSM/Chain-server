package org.example.chain.domain.post.entity;

import jakarta.persistence.*;
import org.example.chain.domain.post.enums.ListType;

import java.util.ArrayList;
import java.util.List;

@Entity
public class ListBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "list_block_id")
    private Long listBlock_id;

    @Enumerated(EnumType.STRING)
    @Column(name = "list_block_type")
    private ListType listBlock_type;

    @OneToMany(mappedBy = "listBlock", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @OrderBy("itemOrder ASC")
    private List<ListItem> listItems = new ArrayList<>();

}