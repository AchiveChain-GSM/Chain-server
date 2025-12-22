package org.example.chain.domain.post.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ImageBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_block_id")
    private Long imageBlock_id;

    @Column(name = "image_key")
    private String imageKey;

    @Column(name = "image_name")
    private String imageName;

    @Column(name = "width")
    private int width;

    @Column(name = "height")
    private int height;

    @Builder
    public ImageBlock(String imageKey, String imageName, int width, int height) {
        this.imageKey = imageKey;
        this.imageName = imageName;
        this.width = width;
        this.height = height;
    }

}