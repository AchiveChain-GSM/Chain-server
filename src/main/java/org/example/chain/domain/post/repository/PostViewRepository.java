package org.example.chain.domain.post.repository;

import org.example.chain.domain.post.entity.Post;
import org.example.chain.domain.post.entity.PostView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostViewRepository extends JpaRepository<PostView, Long> {

    @Query("SELECT p FROM Post p WHERE EXISTS " +
            "(SELECT 1 FROM PostView pv WHERE pv.post = p AND pv.user.id = :user_id)")
    Page<Post> findAllViewedPostsByUserId(Pageable pageable, @Param("user_id") Long user_id);

    @Query("SELECT DISTINCT pv.post FROM PostView pv WHERE pv.user.id = :user_id ORDER BY pv.createAt DESC ")
    Page<Post> findAllPostsByUserIdSortRecentViewed(Pageable pageable, @Param("user_id") Long user_id);

}
