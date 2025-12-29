package org.example.chain.domain.post.repository;

import jakarta.persistence.LockModeType;
import org.example.chain.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.time.Instant;

public interface PostRepository extends JpaRepository<Post, Long> {

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

    // 단건 조회 시 모든 정보를 한 번에 가져옴
    @Query("""
        SELECT DISTINCT p FROM Post p
        LEFT JOIN FETCH p.user
        LEFT JOIN FETCH p.postTags pt
        LEFT JOIN FETCH pt.tag
        WHERE p.id = :postId
    """)
    Optional<Post> findByIdWithDetails(@Param("postId") Long postId);

    @Query(value = "SELECT p FROM Post p JOIN FETCH p.user ORDER BY p.createAt DESC, p.likes DESC")
    Page<Post> findPopularPosts(Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.user.id = :user_id")
    Page<Post> findPostsByUserId(@Param(value = "user_id") Long user_id, Pageable pageable);


    @Query("SELECT p FROM Post p WHERE (p.createAt >= :from) AND (p.createAt <= :to)")
    Page<Post> findAllPostsByCreateAtInDuration(@Param(value = "from") Instant from, @Param(value = "to") Instant to, Pageable pageable);

    // 조회수 중가 로직
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.views = p.views + 1 WHERE p.id = :postId")
    void updateViews(@Param("postId") Long postId);

    // 1. 상세 정보를 한 번에 가져오는 전용 쿼리 (N+1 방지 핵심)
    @Query("""
        SELECT DISTINCT p FROM Post p
        JOIN FETCH p.user
        LEFT JOIN FETCH p.postTags pt
        LEFT JOIN FETCH pt.tag
        WHERE p.id IN :ids
    """)
    List<Post> findAllByIdsWithDetails(@Param("ids") List<Long> ids);

    // 2. 검색 시 ID만 가져오기 (기존 유지)
    @Query("""
        SELECT DISTINCT p.id FROM Post p
        LEFT JOIN p.postTags pt
        LEFT JOIN pt.tag t
        WHERE p.title LIKE CONCAT('%', :keyword, '%')
           OR t.name LIKE CONCAT('%', :keyword, '%')
    """)
    Page<Long> findIdsByIntegratedSearch(@Param("keyword") String keyword, Pageable pageable);

    // 3. 전체 목록 조회 시 ID만 가져오는 메서드 추가
    @Query("SELECT DISTINCT p.id FROM Post p")
    Page<Long> findAllIds(Pageable pageable);

    // 4. 특정 유저의 글 ID만 가져오기
    @Query("SELECT DISTINCT p.id FROM Post p WHERE p.user.id = :userId")
    Page<Long> findIdsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT DISTINCT p FROM Post p WHERE p.id = :postId")
    Optional<Post> findByIdWithLock(@Param("postId") Long postId);

    @Query("SELECT DISTINCT p FROM Post p WHERE p.id = :post_id")
    Optional<Post> findPostById(@Param(value = "post_id") Long post_id);


}