package org.example.chain.domain.post.repository;

import org.example.chain.domain.post.entity.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostTagRepository extends JpaRepository<PostTag, Long> {
}
