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

    // 통합 검색 (N+1 해결 버전)
    public Page<PostReadRes> search(String keyword, Pageable pageable) {
        // [Step 1] 조건에 맞는 ID들만 페이징해서 가져옴 (매우 빠름)
        Page<Long> postIdPage = postRepository.findIdsByIntegratedSearch(keyword, pageable);

        return convertToDtoPage(postIdPage, pageable);
    }

    // 전체 조회 (N+1 해결 버전)
    public Page<PostReadRes> readAllPosts(Pageable pageable) {
        // [Step 1] ID들만 페이징 조회
        Page<Long> postIdPage = postRepository.findAllIds(pageable);

        return convertToDtoPage(postIdPage, pageable);
    }

    // 공통 변환 로직 (2단계 조회)
    private Page<PostReadRes> convertToDtoPage(Page<Long> postIdPage, Pageable pageable) {
        if (postIdPage.isEmpty()) {
            return Page.empty(pageable);
        }

        // [Step 2] 가져온 ID들로 상세 데이터(User, Tag)를 JOIN FETCH로 한 번에 조회
        List<Post> posts = postRepository.findAllByIdsWithDetails(postIdPage.getContent());

        // [Step 3] DTO 변환 (이미 메모리에 데이터가 다 있어서 쿼리 안나감)
        List<PostReadRes> dtos = posts.stream()
                .map(PostReadRes::from)
                .toList();

        return new PageImpl<>(dtos, pageable, postIdPage.getTotalElements());
    }

    @Transactional
    public PostReadRes readPost(Long postId){
        Post post = postRepository.findByIdWithDetails(postId)
                .orElseThrow(() -> new PostNotFoundException("해당 자료를 찾을 수 없습니댜."));

        postRepository.updateViews(postId);
        return PostReadRes.from(post);
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
