package org.example.chain.domain.post.repository;

import org.example.chain.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p ORDER BY FUNCTION('DATE', p.createAt) DESC , p.likes DESC")
    Page<Post> findPopularPost(Pageable pageable);

}
