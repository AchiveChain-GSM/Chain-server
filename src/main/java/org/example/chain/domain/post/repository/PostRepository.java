package org.example.chain.domain.post.repository;

import org.example.chain.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByTitleContaining(String keyword, Pageable pageable);

    @Query("SELECT p FROM Post p " +
            "JOIN p.postTags pt " +
            "JOIN pt.tag t " +
            "WHERE t.name IN :tagNames " +
            "GROUP BY p.id " +
            "HAVING COUNT(DISTINCT t.id) = :tagCount")
    Page<Post> findByAllTagNames(
            @Param("tagNames") List<String> tagNames,
            @Param("tagCount") long tagCount,
            Pageable pageable);

    @Query("""
    SELECT DISTINCT p FROM Post p
    WHERE
        (:keyword IS NOT NULL AND p.title LIKE CONCAT('%', :keyword, '%'))
        OR
        (
            p.id IN (
                SELECT p2.id FROM Post p2
                JOIN p2.postTags pt
                JOIN pt.tag t
                WHERE t.name IN :tagNames
                GROUP BY p2.id
                HAVING COUNT(DISTINCT t.id) = :tagCount
            )
        )
    """)
    Page<Post> findByTitleOrAllTags(
            @Param("keyword") String keyword,
            @Param("tagNames") List<String> tagNames,
            @Param("tagCount") long tagCount,
            Pageable pageable
    );
}
