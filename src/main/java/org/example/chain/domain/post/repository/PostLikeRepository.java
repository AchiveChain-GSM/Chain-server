package org.example.chain.domain.post.repository;

import org.example.chain.domain.post.entity.Post;
import org.example.chain.domain.post.entity.PostLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike,Long> {

    @Query("SELECT pl.post FROM PostLike pl WHERE pl.user.id = :user_id")
    Page<Post> findLikedPostsByUserId(@Param(value = "user_id") Long user_id, Pageable pageable);

}
