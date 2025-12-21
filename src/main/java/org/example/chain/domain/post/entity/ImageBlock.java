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

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "image_name")
    private String imageName;

    @Column(name = "width")
    private int width;

    @Column(name = "height")
    private int height;

    @Builder
    public ImageBlock(String imageUrl, String imageName, int width, int height) {
        this.imageUrl = imageUrl;
        this.imageName = imageName;
        this.width = width;
        this.height = height;
    }

}