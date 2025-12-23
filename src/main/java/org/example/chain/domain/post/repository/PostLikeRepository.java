package org.example.chain.domain.post.repository;

import org.example.chain.domain.post.entity.Post;
import org.example.chain.domain.post.entity.PostLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike,Long> {

    @Query(value = "SELECT pl.post FROM PostLike pl WHERE pl.user.id = :user_id",
    countQuery = "SELECT COUNT(pl) FROM PostLike pl WHERE pl.id = :user_id")
    Page<Post> findLikedPostsByUserId(@Param(value = "user_id") Long user_id, Pageable pageable);

    Optional<PostLike> findByPostIdAndUserId(Long postId, Long userId);

    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.post.id = :postId")
    long countByPostId(@Param("postId") Long postId);

    @Query("SELECT DISTINCT pl.post.id FROM PostLike pl WHERE pl.user.id = :user_id")
    Set<Long> findLikedPostIds(@Param(value = "user_id") Long userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.likes = p.likes + 1 WHERE p.id = :postId")
    void addLikes(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.likes = p.likes - 1 WHERE p.id = :postId AND p.likes > 0")
    void minusLikes(@Param("postId") Long postId);

}
