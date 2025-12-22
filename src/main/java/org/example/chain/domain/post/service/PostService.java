package org.example.chain.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.example.chain.domain.post.entity.PostBookmark;
import org.example.chain.domain.post.data.req.PostCreateReq;
import org.example.chain.domain.post.data.res.PostReadRes;
import org.example.chain.domain.post.entity.*;
import org.example.chain.domain.post.data.res.*;
import org.example.chain.domain.post.repository.PostBookmarkRepository;
import org.example.chain.domain.post.repository.PostLikeRepository;
import org.example.chain.domain.post.repository.PostRepository;
import org.example.chain.domain.post.repository.PostViewRepository;
import org.example.chain.domain.user.entity.User;
import org.example.chain.global.error.exception.PostNotFoundException;
import org.example.chain.global.s3.S3Service;
import org.example.chain.global.security.util.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.List;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final TagService tagService;
    private final SecurityUtil securityUtil;
    private final PostViewRepository postViewRepository;
    private final PostLikeRepository postLikeRepository;
    private final S3Service s3Service;
    private final PostBookmarkRepository bookmarkRepository;

    @Transactional
    public Long createPost(PostCreateReq request){
        Post post = Post.builder()
                .title(request.title())
                .content(request.content())
                .description(request.description())
                .user(securityUtil.getCurrentUser())
                .build();

        List<PostTag> postTags = request.tags().stream()
                .map(tagService::getOrCreateTag) // TagService의 메서드 호출
                .map(tag -> new PostTag(post, tag))
                .toList();

        post.getPostTags().addAll(postTags);

        List<Image> images = new ArrayList<>();
        for(var imageFile : request.images()) {

            String imageKey = s3Service.upload(imageFile, "posts");

            Image image = Image.builder()
                    .imageKey(imageKey)
                    .imageName(imageFile.getOriginalFilename())
                    .build();
            images.add(image);
        }
        post.getImages().addAll(images);

        return postRepository.save(post).getId();
    }

    List<String> getImageUrls(Post post) {
        return post.getImages().stream().map(image -> {
            return s3Service.generateGetUrl(image.getImageKey());
        }).toList();
    }

    String getFirstImageUrl(Post post) {
        return s3Service.generateGetUrl(post.getImages().getFirst().getImageKey());
    }

    boolean isLiked(Long postId, Long userId) {
        return postLikeRepository.existsByPostIdAndUserId(postId, userId);
    }

    boolean isBookmarked(Long postId, Long userId) {
        return postLikeRepository.existsByPostIdAndUserId(postId, userId);
    }



    @Transactional
    // 통합 검색 (N+1 해결 버전)
    public Page<PostReadRes> search(String keyword, Pageable pageable, Long user_id) {
        // [Step 1] 조건에 맞는 ID들만 페이징해서 가져옴 (매우 빠름)
        Page<Long> postIdPage = postRepository.findIdsByIntegratedSearch(keyword, pageable);

        return convertToDtoPage(postIdPage, pageable,  user_id);
    }

    @Transactional
    // 전체 조회 (N+1 해결 버전)
    public Page<PostReadRes> readAllPosts(Pageable pageable, Long user_id) {
        // [Step 1] ID들만 페이징 조회
        Page<Long> postIdPage = postRepository.findAllIds(pageable);

        return convertToDtoPage(postIdPage, pageable,  user_id);
    }

    // 공통 변환 로직 (2단계 조회)
    private Page<PostReadRes> convertToDtoPage(Page<Long> postIdPage, Pageable pageable, Long user_id) {
        if (postIdPage.isEmpty()) {
            return Page.empty(pageable);
        }

        // [Step 2] 가져온 ID들로 상세 데이터(User, Tag)를 JOIN FETCH로 한 번에 조회
        List<Post> posts = postRepository.findAllByIdsWithDetails(postIdPage.getContent());
        java.util.Map<Long, Post> postMap = posts.stream()
                .collect(java.util.stream.Collectors.toMap(Post::getId, p -> p));

        // [Step 3] DTO 변환 (이미 메모리에 데이터가 다 있어서 쿼리 안나감)
        List<PostReadRes> dto = postIdPage.getContent().stream()
                .map(postMap::get)
                .map(post -> {
                    return PostReadRes.from(post, getFirstImageUrl(post), isLiked(post.getId(), user_id), isBookmarked(post.getId(), user_id));
                })
                .toList();

        return new PageImpl<>(dto, pageable, postIdPage.getTotalElements());
    }
    @Transactional
    public PostDetailReadRes readPost(Long postId){
        Post post = postRepository.findByIdWithDetails(postId)
                .orElseThrow(() -> new PostNotFoundException("해당 자료를 찾을 수 없습니댜."));

        postRepository.updateViews(postId);
        return PostDetailReadRes.from(post, getImageUrls(post));
    }

    @Transactional(readOnly = true)
    public Page<PostReadRes> readAllViewedPosts(Pageable pageable, Long user_id){
        return postViewRepository.findAllViewedPostsByUserId(pageable ,user_id)
                .map(post -> {
                    return PostReadRes.from(post, getFirstImageUrl(post), isLiked(post.getId(), user_id), isBookmarked(post.getId(), user_id));
                });
    }

    @Transactional(readOnly = true)
    public Page<PostReadRes> readAllViewedPostsSortRecentViewed(Pageable pageable, Long user_id){
        return postViewRepository.findAllPostsByUserIdSortRecentViewed(pageable, user_id)
                .map(post -> {
                    return PostReadRes.from(post, getFirstImageUrl(post), isLiked(post.getId(), user_id), isBookmarked(post.getId(), user_id));
                });
    }
    @Transactional(readOnly = true)
    public Page<PostReadRes> readPopularPosts(Pageable pageable, Long user_id) {
        return postRepository.findPopularPosts(pageable)
                .map(post -> {
                    return PostReadRes.from(post, getFirstImageUrl(post), isLiked(post.getId(), user_id), isBookmarked(post.getId(), user_id));
                });
    }
    @Transactional(readOnly = true)
    public Page<PostReadRes> readAllWrittenPosts(Pageable pageable, Long user_id){
        return postRepository.findPostsByUserId(user_id, pageable)
                .map(post -> {
                    return PostReadRes.from(post, getFirstImageUrl(post), isLiked(post.getId(), user_id), isBookmarked(post.getId(), user_id));
                });
    }
    @Transactional(readOnly = true)
    public Page<PostReadRes> readAllLikedPosts(Pageable pageable, Long user_id){
        return postLikeRepository.findLikedPostsByUserId(user_id, pageable)
                .map(post -> {
                    return PostReadRes.from(post, getFirstImageUrl(post), isLiked(post.getId(), user_id), isBookmarked(post.getId(), user_id));
                });
    }

    @Transactional(readOnly = true)
    public Page<PostReadRes> readAllPostsInDuration(Instant from, Instant to, Pageable pageable){
        return postRepository.findAllPostsByCreateAtInDuration(from, to, pageable)
                .map(post -> {
                    return PostReadRes.from(post, getFirstImageUrl(post), false, false);
                });
    }

    @Transactional
    public void toggleLike(Long postId) {
        User user = securityUtil.getCurrentUser();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("게시글 없음"));

        Optional<PostLike> postLikeOptional = postLikeRepository.findByPostIdAndUserId(postId, user.getId());

        if (postLikeOptional.isPresent()) {
            postLikeRepository.delete(postLikeOptional.get());
            postLikeRepository.addLikes(postId);
        } else {
            PostLike newLike = new PostLike(post, user);
            postLikeRepository.save(newLike);
            postLikeRepository.minusLikes(postId);
        }
    }

    @Transactional
    public void toggleBookmark(Long postId) {
        User user = securityUtil.getCurrentUser();
        Post post = postRepository.findByIdWithLock(postId)
                .orElseThrow(PostNotFoundException::new);

        bookmarkRepository.findByPostIdAndUserId(postId, user.getId())
                .ifPresentOrElse(
                        bookmark -> {
                            bookmarkRepository.delete(bookmark);
                            bookmarkRepository.minusBookmark(postId);
                        },
                        () -> {
                            bookmarkRepository.save(new PostBookmark(post, user));
                            bookmarkRepository.addBookmark(postId);
                        }
                );
    }
}
