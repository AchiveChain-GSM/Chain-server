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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final TagRepository tagRepository;

    @Transactional
    public Long createPost(PostCreateReq request){
        Post post = Post.builder()
                .title(request.title())
                .content(request.content())
                .build();

        // 태그 처리 로직
        List<PostTag> postTags = request.tags().stream()
                .map(tagName -> tagRepository.findByName(tagName)
                        .orElseGet(() -> tagRepository.save(new Tag(tagName)))) // 없으면 생성
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
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("해당 자료를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        return PostReadRes.builder()
                .title(post.getTitle())
                .content(post.getContent())
                .likes(post.getLikes())
                .comments(post.getComments())
                .build();
    }

    @Transactional(readOnly = true)
    public Page<PostReadRes> readAllPosts(Pageable pageable){
        return postRepository.findAll(pageable)
                .map(PostReadRes::from);
    }

    public Page<PostReadRes> search(
            String keyword,
            List<String> tagNames,
            Pageable pageable
    ) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasTags = tagNames != null && !tagNames.isEmpty();

        if (hasKeyword && hasTags) { // 둘다 안비었음
            return postRepository
                    .findByTitleOrAllTags(
                            keyword,
                            tagNames,
                            tagNames.size(),
                            pageable
                    )
                    .map(PostReadRes::from);
        }

        if (hasKeyword) {
            return postRepository
                    .findByTitleContaining(keyword, pageable)
                    .map(PostReadRes::from);
        }

        if (hasTags) {
            return postRepository
                    .findByAllTagNames(tagNames, tagNames.size(), pageable)
                    .map(PostReadRes::from);
        }

        return postRepository.findAll(pageable)
                .map(PostReadRes::from);
    }
}
