package org.example.chain.domain.post.repository;

import org.example.chain.domain.post.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    @Query("""
        SELECT c FROM PostComment c
        JOIN FETCH c.user
        WHERE c.post.id = :postId
        ORDER BY c.createAt DESC
    """)
    List<PostComment> findAllByPostId(@Param("postId") Long postId);
}
