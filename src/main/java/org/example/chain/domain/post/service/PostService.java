package org.example.chain.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.example.chain.domain.post.data.req.PostReportReq;
import org.example.chain.domain.post.data.req.PostUpdateReq;
import org.example.chain.domain.post.entity.PostBookmark;
import org.example.chain.domain.post.data.req.PostCreateReq;
import org.example.chain.domain.post.data.res.PostReadRes;
import org.example.chain.domain.post.entity.*;
import org.example.chain.domain.post.data.res.*;
import org.example.chain.domain.post.repository.*;
import org.example.chain.domain.user.entity.User;
import org.example.chain.domain.user.repository.UserRepository;
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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final TagService tagService;
    private final SecurityUtil securityUtil;
    private final PostViewRepository postViewRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostReportRepository postReportRepository;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final PostBookmarkRepository bookmarkRepository;
    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;

    @Transactional
    public Long createPost(PostCreateReq request){
        User user = securityUtil.getCurrentUser();

        Post post = Post.builder()
                .title(request.title())
                .content(request.content())
                .user(user)
                .build();

        List<PostTag> postTags = request.tags().stream()
                .map(tagService::getOrCreateTag) // TagService의 메서드 호출
                .map(tag -> {
                    PostTag postTag = new PostTag(post, tag);
                    postTagRepository.save(postTag);
                    return postTag;
                })
                .toList();

        post.getPostTags().addAll(postTags);

        List<Image> images = new ArrayList<>();
        for(var imageFile : request.images()) {

            String imageKey = s3Service.upload(imageFile, user.getId().toString());

            Image image = Image.builder()
                    .imageKey(imageKey)
                    .imageName(imageFile.getOriginalFilename())
                    .post(post)
                    .build();
            imageRepository.save(image);

            images.add(image);
        }
        post.getImages().addAll(images);

        return postRepository.save(post).getId();
    }

    Map<Long, String> getImageUrls(Post post) {
        return post.getImages().stream().collect(Collectors.toMap(Image::getImage_id, image -> s3Service.generateGetUrl(image.getImageKey())));
    }

    String getFirstImageUrl(Post post) {
        if (post.getImages() == null || post.getImages().isEmpty()) {
            return null; // 혹은 기본 이미지 URL
        }
        return s3Service.generateGetUrl(post.getImages().getFirst().getImageKey());
    }

    boolean isLiked(Long postId, Long userId) {
        return postLikeRepository.existsByPostIdAndUserId(postId, userId);
    }

    boolean isBookmarked(Long postId, Long userId) {
        return postLikeRepository.existsByPostIdAndUserId(postId, userId);
    }

    @Transactional
    public void reportPost(PostReportReq postReportReq, Long user_id) {

        Post post = postRepository.findPostById(postReportReq.post_id());
        User user = userRepository.getById(user_id);

        PostReport postReport = PostReport.builder()
                        .title(postReportReq.title())
                        .description(postReportReq.description())
                        .post(post)
                        .user(user)
                        .build();

        post.getPostReports().add(postReport);

        postReportRepository.save(postReport);

    }

    @Transactional(readOnly = true)
    public Page<PostReportRes> readReport(Pageable pageable, Long user_id) {

        Page<PostReport> postReport = postReportRepository.findPostReportByUserId(user_id, pageable);
        return postReport.map(pr -> {
            return PostReportRes.builder()
                    .post_id(pr.getPost().getId())
                    .title(pr.getTitle())
                    .description(pr.getDescription())
                    .build();
        });
    }


    @Transactional
    public void deleteReport(Long postReport_id) {

        Post post = postRepository.findPostById(postReport_id);

        PostReport postReport = post.getPostReports().stream()
                .filter(pr -> pr.getReport_id().equals(postReport_id))
                .findFirst()
                .orElseThrow();

        post.getPostReports().remove(postReport);

    }

    @Transactional
    public void updatePost(PostUpdateReq postUpdateReq) {
        User user = securityUtil.getCurrentUser();
        // 1. 게시글 조회
        Post post = postRepository.findById(postUpdateReq.post_id())
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다."));

        // 2. 기본 정보 업데이트 (제목, 내용 등)
        post.updatePost(postUpdateReq);

        // 3. 태그 업데이트 (null 체크 추가)
        if (postUpdateReq.tags() != null) {
            post.getPostTags().clear();
            for (String tagName : postUpdateReq.tags()) {
                Tag tag = tagRepository.findByName(tagName).orElseGet(() -> {
                    Tag t = new Tag(tagName);
                    return tagRepository.save(t);
                });
                PostTag postTag = new PostTag(post, tag);
                postTagRepository.save(postTag);
                post.getPostTags().add(postTag);
            }
        }

        // 4. 기존 이미지 삭제 (null 체크 추가)
        if (postUpdateReq.removeImage_ids() != null && !postUpdateReq.removeImage_ids().isEmpty()) {
            removeImagesById(postUpdateReq.removeImage_ids());
        }

        // 5. 새로운 이미지 추가 (null 및 빈 파일 체크 추가)
        if (postUpdateReq.images() != null && !postUpdateReq.images().isEmpty()) {
            for (var imageFile : postUpdateReq.images()) {
                if (imageFile.isEmpty()) continue; // 실제 파일이 있는지 확인

                String imageKey = s3Service.upload(imageFile, user.getId().toString());
                Image image = Image.builder()
                        .imageKey(imageKey)
                        .imageName(imageFile.getOriginalFilename())
                        .post(post) // 연관 관계 설정
                        .build();

                imageRepository.save(image);
                post.getImages().add(image); // 객체 상태 동기화
            }
        }
    }

    void removeImagesById (List<Long> image_ids) {
        for(var remove_id : image_ids) {
            Image image = imageRepository.findByImage_id(remove_id);
            s3Service.delete(image.getImageKey());
            imageRepository.delete(image);
        }
    }


    public void deletePost(Long post_id) {

        Post post = postRepository.getPostById(post_id);

        removeImagesById(post.getImages().stream()
                .map(Image::getImage_id).toList());
        postRepository.delete(post);

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

        return convertToDtoPage(postIdPage, pageable, user_id);
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
