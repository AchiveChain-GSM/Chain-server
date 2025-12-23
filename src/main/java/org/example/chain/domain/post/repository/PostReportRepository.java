package org.example.chain.domain.post.repository;

import org.example.chain.domain.post.entity.Post;
import org.example.chain.domain.post.entity.PostReport;
import org.example.chain.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostReportRepository extends JpaRepository<PostReport,Long> {
    List<PostReport> user(User user);

    @Query("SELECT pr FROM PostReport pr JOIN FETCH pr.post WHERE pr.user.id = :userId")
    Page<PostReport> findPostReportByUserId(@Param(value = "user_id") Long user_id, Pageable pageable);

    @Query("SELECT pr.post FROM PostReport pr WHERE pr.report_id = :report_id")
    Post findReportPostByPost_id(@Param(value = "report_id") Long report_id);
}


