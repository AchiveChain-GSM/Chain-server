package org.example.chain.domain.post.repository;

import org.example.chain.domain.post.entity.PostBookmark;
import org.example.chain.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

public interface PostBookmarkRepository extends JpaRepository<PostBookmark, Long> {

    Optional<PostBookmark> findByPostIdAndUserId(Long postId, Long userId);

    @Query(value = "SELECT pb.post FROM PostBookmark pb WHERE pb.user.id = :userId",
            countQuery = "SELECT COUNT(pb.post) FROM PostBookmark pb WHERE pb.user.id = :userId")
    Page<Post> findBookmarkedPosts(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT pb.post.id FROM PostBookmark pb WHERE pb.user.id = :userId")
    Set<Long> findBookmarkedPostIds(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.bookmarks = p.bookmarks + 1 WHERE p.id = :postId")
    void addBookmark(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.bookmarks = p.bookmarks - 1 WHERE p.id = :postId")
    void minusBookmark(@Param("postId") Long postId);
}
