package org.example.chain.domain.post.repository;

import jakarta.persistence.LockModeType;
import org.example.chain.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import java.time.Instant;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByTitleContaining(String keyword, Pageable pageable);

    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.user " + // 다대일(User)은 페치 조인해도 페이징 가능!
            "WHERE p.id IN (" +
            "  SELECT p2.id FROM Post p2 " +
            "  JOIN p2.postTags pt2 " +
            "  JOIN pt2.tag t2 " +
            "  WHERE t2.name IN :tagNames " +
            "  GROUP BY p2.id " +
            "  HAVING COUNT(DISTINCT t2.id) = :tagCount" +
            ")")
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

    @Query("SELECT p.id FROM Post p " +
            "JOIN p.postTags pt " +
            "JOIN pt.tag t " +
            "WHERE t.name IN :tagNames " +
            "GROUP BY p.id " +
            "HAVING COUNT(DISTINCT t.id) = :tagCount")
    Page<Long> findIdsByTags(@Param("tagNames") List<String> tagNames,
                             @Param("tagCount") long tagCount,
                             Pageable pageable);

    // 단건 조회 시 모든 정보를 한 번에 가져옴
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.user " +
            "LEFT JOIN FETCH p.postTags pt " +
            "LEFT JOIN FETCH pt.tag " +
            "WHERE p.id = :postId")
    Optional<Post> findByIdWithDetails(@Param("postId") Long postId);

    // ID 목록을 받아 상세 정보(User, Tag 등)를 한 번에 가져오는 쿼리 (N+1 방지 핵심)
    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.user " +
            "LEFT JOIN FETCH p.postTags pt " +
            "LEFT JOIN FETCH pt.tag " +
            "WHERE p.id IN :ids")
    List<Post> findAllByIdsWithDetails(@Param("ids") List<Long> ids);

    // 검색 조건에 맞는 'ID'만 페이징해서 가져오는 쿼리 (페이징 성능 최적화)
    @Query("""
    SELECT DISTINCT p.id FROM Post p
    WHERE 
        (:keyword IS NOT NULL AND p.title LIKE CONCAT('%', :keyword, '%'))
        OR 
        p.id IN (
            SELECT p2.id FROM Post p2
            JOIN p2.postTags pt2
            JOIN pt2.tag t2
            WHERE t2.name IN :tagNames
            GROUP BY p2.id
            HAVING COUNT(DISTINCT t2.id) = :tagCount
        )
""")
    Page<Long> findIdsBySearchCondition(
            @Param("keyword") String keyword,
            @Param("tagNames") List<String> tagNames,
            @Param("tagCount") Long tagCount,
            Pageable pageable
    );

    @Query(value = "SELECT p FROM Post p JOIN p.user ORDER BY FUNCTION('DATE', p.createAt) DESC , p.likes DESC",
            countQuery = "SELECT count(p) FROM Post p")
    Page<Post> findPopularPosts(Pageable pageable);

    @Query(value = "SELECT p FROM Post p JOIN p.user u WHERE u.name = :name",
            countQuery = "SELECT COUNT(p) FROM Post p JOIN p.user u WHERE u.name = :name")  //해당하는 이름의 작성자의 post 조회
    Page<Post> findPostsByUserName(@Param("name") String name, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "SELECT p FROM Post p WHERE p.id = :post_id")  //post_id를 통한 post 조회
    Optional<Post> findPostByPostId(@Param("post_id") Long post_id);

    @Query("SELECT p FROM Post p WHERE p.user.id = :user_id")
    Page<Post> findPostsByUserId(@Param(value = "user_id") Long user_id, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE (p.createAt >= :from) AND (p.createAt <= :to)")
    Page<Post> findAllPostsByCreateAtInDuration(@Param(value = "from") Instant from, @Param(value = "to") Instant to, Pageable pageable);


}
