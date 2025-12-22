package org.example.chain.domain.post.repository;

import org.example.chain.domain.post.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ImageRepository extends JpaRepository<Image,Long> {

    @Query("SELECT i FROM Image i WHERE i.image_id = :image_id")
    Image findByImage_id(@Param(value = "image_id") Long image_id);

}
