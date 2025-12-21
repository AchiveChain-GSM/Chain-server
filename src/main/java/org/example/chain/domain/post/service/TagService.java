package org.example.chain.domain.post.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.chain.domain.post.entity.Tag;
import org.example.chain.domain.post.repository.TagRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
@RequiredArgsConstructor
public class TagService { // 별도 서비스로 분리 권장

    private final TagRepository tagRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW) // 독립된 트랜잭션 사용
    public Tag getOrCreateTag(String tagName) {
        return tagRepository.findByName(tagName)
                .orElseGet(() -> {
                    try {
                        return tagRepository.saveAndFlush(new Tag(tagName));
                    } catch (DataIntegrityViolationException e) {
                        return tagRepository.findByName(tagName).orElseThrow();
                    }
                });
    }
}