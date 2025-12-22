package org.example.chain.domain.post.repository;

import org.example.chain.domain.post.entity.PostBookmark;
import org.example.chain.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostBookmarkRepository extends JpaRepository<PostBookmark, Long> {

    Optional<PostBookmark> findByPostIdAndUserId(Long postId, Long userId);

    @Query("SELECT pb.post FROM PostBookmark pb WHERE pb.user.id = :userId")
    Page<Post> findBookmarkedPosts(@Param("userId") Long userId, Pageable pageable);
}
