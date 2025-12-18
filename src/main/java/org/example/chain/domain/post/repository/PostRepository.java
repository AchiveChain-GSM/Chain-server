package org.example.chain.domain.post.repository;

import jakarta.persistence.LockModeType;
import org.example.chain.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p JOIN FETCH p.user ORDER BY FUNCTION('DATE', p.createAt) DESC , p.likes DESC")
    Page<Post> findPopularPosts(Pageable pageable);

    @Query(value = "SELECT p FROM Post p JOIN FETCH p.user u WHERE u.name = :name",
            countQuery = "SELECT COUNT(p) FROM Post p")  //해당하는 이름의 작성자의 post 조회
    Page<Post> findPostsByUserName(@Param("name") String name, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "SELECT p FROM Post p WHERE p.id = :post_id")  //post_id를 통한 post 조회
    Optional<Post> findPostByPostId(@Param("post_id") Long post_id);


}
