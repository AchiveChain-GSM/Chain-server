package org.example.chain.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.example.chain.domain.post.data.req.PostCreateReq;
import org.example.chain.domain.post.data.res.PostReadRes;
import org.example.chain.domain.post.entity.Post;
import org.example.chain.domain.post.entity.PostTag;
import org.example.chain.domain.post.repository.PostLikeRepository;
import org.example.chain.domain.post.repository.PostRepository;
import org.example.chain.domain.post.repository.PostViewRepository;
import org.example.chain.global.error.exception.PostNotFoundException;
import org.example.chain.global.security.util.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final TagService tagService;
    private final SecurityUtil securityUtil;
    private final PostViewRepository postViewRepository;
    private final PostLikeRepository postLikeRepository;

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

    @Transactional
    public PostReadRes readPost(Long postId){
        postRepository.updateViews(postId);

        Post post = postRepository.findByIdWithDetails(postId)
                .orElseThrow(() -> new PostNotFoundException("해당 자료를 찾을 수 없습니댜."));
        return PostReadRes.from(post);
    }

    @Transactional(readOnly = true)
    public Page<PostReadRes> readAllPosts(Pageable pageable){
        return postRepository.findAll(pageable)
                .map(PostReadRes::from);
    }

    @Transactional(readOnly = true)
    public Page<PostReadRes> search(String keyword, Pageable pageable) {
        // 1. 키워드가 없으면 전체 조회, 있으면 통합 검색 수행
        Page<Long> postIdPage;

        if (keyword == null || keyword.isBlank()) {
            postIdPage = postRepository.findAll(pageable).map(Post::getId);
        } else {
            postIdPage = postRepository.findIdsByIntegratedSearch(keyword, pageable);
        }

        if (postIdPage.isEmpty()) {
            return Page.empty(pageable);
        }

        // 2. 검색된 ID들로 상세 정보(User, Tag) 페치 조인 조회
        List<Post> posts = postRepository.findAllByIdsWithDetails(postIdPage.getContent());

        // 3. Pageable 객체 내의 정렬 정보를 유지하며 DTO 변환
        // (주의: findByIds는 순서가 섞일 수 있으므로 정렬이 중요하다면 추가 처리가 필요할 수 있습니다.)
        List<PostReadRes> dtos = posts.stream()
                .map(PostReadRes::from)
                .toList();

        return new PageImpl<>(dtos, pageable, postIdPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<PostReadRes> readPopularPosts(Pageable pageable){
        return postRepository.findPopularPosts(pageable)
                .map(PostReadRes::from);
    }
    @Transactional(readOnly = true)
    public Page<PostReadRes> readAllViewedPosts(Pageable pageable, Long user_id){
        return postViewRepository.findAllViewedPostsByUserId(pageable ,user_id)
                .map(PostReadRes::from);
    }
    @Transactional(readOnly = true)
    public Page<PostReadRes> readAllViewedPostsSortRecentViewed(Pageable pageable, Long user_id){
        return postViewRepository.findAllPostsByUserIdSortRecentViewed(pageable, user_id)
                .map(PostReadRes::from);
    }
    @Transactional(readOnly = true)
    public Page<PostReadRes> readAllWrittenPosts(Pageable pageable, Long user_id){
        return postRepository.findPostsByUserId(user_id, pageable)
                .map(PostReadRes::from);
    }
    @Transactional(readOnly = true)
    public Page<PostReadRes> readAllLikedPosts(Pageable pageable, Long user_id){
        return postLikeRepository.findLikedPostsByUserId(user_id, pageable)
                .map(PostReadRes::from);
    }

    @Transactional(readOnly = true)
    public Page<PostReadRes> readAllPostsInDuration(Instant from, Instant to, Pageable pageable){
        return postRepository.findAllPostsByCreateAtInDuration(from, to, pageable)
                .map(PostReadRes::from);
    }
}
