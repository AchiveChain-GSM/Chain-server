package org.example.chain.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.example.chain.domain.post.data.req.PostCreateReq;
import org.example.chain.domain.post.data.res.PostReadRes;
import org.example.chain.domain.post.entity.Post;
import org.example.chain.domain.post.entity.PostTag;
import org.example.chain.domain.post.entity.Tag;
import org.example.chain.domain.post.repository.PostRepository;
import org.example.chain.domain.post.repository.TagRepository;
import org.example.chain.global.error.exception.PostNotFoundException;
import org.example.chain.global.security.util.SecurityUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final TagService tagService;
    private final SecurityUtil securityUtil;

    @Transactional
    public Long createPost(PostCreateReq request){
        Post post = Post.builder()
                .title(request.title())
                .content(request.content())
                .user(securityUtil.getCurrentUser())
                .build();

        List<PostTag> postTags = request.tags().stream()
                .map(tagService::getOrCreateTag) // TagService의 메서드 호출
                .map(tag -> new PostTag(post, tag))
                .toList();

        post.getPostTags().addAll(postTags);
        return postRepository.save(post).getId();
    }

    @Transactional(readOnly = true)
    public Page<PostReadRes> searchByTags(List<String> tagNames, Pageable pageable) {
        if (tagNames == null || tagNames.isEmpty()) {
            return postRepository.findAll(pageable).map(PostReadRes::from);
        }

        return postRepository.findByAllTagNames(tagNames, (long) tagNames.size(), pageable)
                .map(PostReadRes::from);
    }

    @Transactional(readOnly = true)
    public PostReadRes readPost(Long postId){
        Post post = postRepository.findByIdWithDetails(postId) // Fetch Join 버전 사용
                .orElseThrow(() -> new PostNotFoundException("해당 자료를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        return PostReadRes.from(post);
    }

    @Transactional(readOnly = true)
    public Page<PostReadRes> readAllPosts(Pageable pageable){
        return postRepository.findAll(pageable)
                .map(PostReadRes::from);
    }

    @Transactional(readOnly = true)
    public Page<PostReadRes> search(String keyword, List<String> tagNames, Pageable pageable) {
        String searchKeyword = (keyword != null && !keyword.isBlank()) ? keyword : null;
        List<String> searchTags = (tagNames != null && !tagNames.isEmpty()) ? tagNames : null;
        Long tagCount = (searchTags != null) ? (long) searchTags.size() : 0L;

        Page<Long> postIdPage = postRepository.findIdsBySearchCondition(
                searchKeyword, searchTags, tagCount, pageable
        );

        if (postIdPage.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Post> posts = postRepository.findAllByIdsWithDetails(postIdPage.getContent());

        List<PostReadRes> dto = posts.stream()
                .map(PostReadRes::from)
                .toList();

        return new PageImpl<>(dto, pageable, postIdPage.getTotalElements());
    }
}
