package org.example.chain.domain.post.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.chain.domain.post.data.req.PostReportReq;
import org.example.chain.domain.post.data.req.PostUpdateReq;
import org.example.chain.domain.post.entity.PostBookmark;
import org.example.chain.domain.post.data.req.PostCreateReq;
import org.example.chain.domain.post.data.res.PostReadRes;
import org.example.chain.domain.post.entity.*;
import org.example.chain.domain.post.data.res.*;
import org.example.chain.domain.post.repository.*;
import org.example.chain.domain.user.entity.User;
import org.example.chain.global.error.exception.PostNotFoundException;
import org.example.chain.global.s3.S3Service;
import org.example.chain.global.security.util.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.util.*;
import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
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
    private final PostCommentRepository postCommentRepository;
    private final TagRepository tagRepository;
    private final S3Service s3Service;
    private final PostBookmarkRepository bookmarkRepository;
    private final PostTagRepository postTagRepository;
    private final PostBookmarkRepository postBookmarkRepository;
    private final EntityManager entityManager;

    //게시물 생성
    @Transactional
    public Long createPost(PostCreateReq request){
        User user = securityUtil.getCurrentUser();

        //제목, 내용, 작성자 추가
        Post post = Post.builder()
                .title(request.title())
                .content(request.content())
                .user(user)
                .images(new ArrayList<>())
                .postTags(new HashSet<>())
                .build();

        Post savedPost = postRepository.save(post);

        if (request.tags() != null && !request.tags().isEmpty()) {
            processTags(savedPost, request.tags());
        }

        // 3. 이미지 S3 업로드 및 저장
        if (request.images() != null && !request.images().isEmpty()) {
            uploadImages(savedPost, request.images());
        }

        return savedPost.getId();
    }



    @Transactional
    public void updatePost(PostUpdateReq req) {
        // 1. 대상 게시물 조회
        Post post = postRepository.findById(req.post_id())
                .orElseThrow(() -> new PostNotFoundException("수정할 게시글을 찾을 수 없습니다."));

        // 2. 제목, 내용 수정
        post.updatePost(req);

        // 3. 태그 수정 (기존 관계 제거 후 새 관계 설정)
        if (req.tags() != null) {
            processTags(post, req.tags());
        }

        // 4. 선택적 이미지 삭제 로직
        if (req.removeImageIds() != null && !req.removeImageIds().isEmpty()) {
            List<Image> currentImages = new ArrayList<>(post.getImages());
            for (Image img : currentImages) {
                if (req.removeImageIds().contains(img.getImage_id())) {
                    s3Service.delete(img.getImageKey()); // S3에서 파일 삭제
                    post.removeImage(img);               // Post 엔티티 연관관계 제거
                }
            }
        }

        // 5. 신규 이미지 추가
        if (req.newImages() != null && !req.newImages().isEmpty()) {
            uploadImages(post, req.newImages());
        }

        // @Transactional에 의해 메서드 종료 시 영속성 컨텍스트의 변경 내용이 DB에 반영(Flush)됩니다.
    }


    //게시물(Post) 삭제할 때 무조건 이거 사용하세요!
    //removeImageById를 통해 S3에 있는 image 까지 삭제해야 합니다.
    @Transactional
    public void deletePost(Long post_id) {
        Post post = postRepository.findById(post_id)
                .orElseThrow(() -> new PostNotFoundException("이미 삭제되었거나 존재하지 않는 게시글입니다."));

        removeImagesInPost(post, post.getImages());
        postRepository.delete(post);
    }

    //게시물 내 삭제할 이미지 목록을 가지고 S3 내에서도, 게시물 자체에서도 삭제
    void removeImagesInPost(Post post, List<Image> removeImages) {
        for(var removeImage : removeImages) {
            s3Service.delete(removeImage.getImageKey());
        }
        post.getImages().clear();
    }

    //상세 게시물 조회시 사용, 게시물 안에 있는 image url 모두 불러오기
    Map<Long, String> getImageUrls(Post post) {
        return post.getImages().stream().collect(Collectors.toMap(Image::getImage_id, image -> s3Service.generateGetUrl(image.getImageKey())));
    }

    //조회 게시물 조회시 사용, 맨 상단에 있는 image url 하나만 조회
    String getFirstImageUrl(Post post) {
        return post.getImages().stream()
                .findFirst() // 첫 번째 요소가 있으면 가져오고 없으면 빈 Optional 반환
                .map(image -> s3Service.generateGetUrl(image.getImageKey()))
                .orElse(null); // 이미지가 없으면 null 반환 (또는 기본 이미지 URL)
    }


    @Transactional
    // 통합 검색 (N+1 해결 버전)
    public Page<PostReadRes> search(String keyword, Pageable pageable) {
        // [Step 1] 조건에 맞는 ID들만 페이징해서 가져옴 (매우 빠름)
        Page<Long> postIdPage = postRepository.findIdsByIntegratedSearch(keyword, pageable);

        log.info("검색 완료");
        return convertToDtoPage(postIdPage, pageable);
    }

    @Transactional
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
        java.util.Map<Long, Post> postMap = posts.stream()
                .collect(java.util.stream.Collectors.toMap(Post::getId, p -> p));

        User user = securityUtil.getCurrentUser();

        Set<Long> likedIds = likedPostIds(user.getId());
        Set<Long> bookmarkedIds = bookmarkedPostIds(user.getId());

        // [Step 3] DTO 변환 (이미 메모리에 데이터가 다 있어서 쿼리 안나감)
        List<PostReadRes> dto = postIdPage.getContent().stream()
                .map(postMap::get)
                .map(post -> {
                    return PostReadRes.from(post, getFirstImageUrl(post), isLiked(post.getId(), likedIds), isBookmarked(post.getId(), bookmarkedIds));
                })
                .toList();

        return new PageImpl<>(dto, pageable, postIdPage.getTotalElements());
    }
    //게시물 ID를 가지고 해당하는 게시물 상세 내용을 조회
    @Transactional
    public PostDetailReadRes readPost(Long postId){

        //게시물 상세 조회
        Post post = postRepository.findByIdWithDetails(postId)
                .orElseThrow(() -> new PostNotFoundException("해당 자료를 찾을 수 없습니댜."));

        List<PostComment> comments = postCommentRepository.findByPostId(postId);
        Map<Long, String> images = getImageUrls(post);

        //조회수 증가
        postRepository.updateViews(postId);

        long likes = postLikeRepository.countByPost(post);
        long bookmarks = postBookmarkRepository.countByPost(post);

        User user = securityUtil.getCurrentUser();

        if (user != null) {
            try {
                PostView view = PostView.builder()
                        .post(post)
                        .user(user)
                        .build();
                postViewRepository.save(view);
            } catch (Exception e) {
                log.error("최근 본 자료 저장 실패", e);
            }
        }

        boolean isLiked = false;
        boolean isBookmarked = false;

        // 기존에 만들어둔 헬퍼 메서드 활용
        Set<Long> likedIds = likedPostIds(user.getId());
        Set<Long> bookmarkedIds = bookmarkedPostIds(user.getId());

        isLiked = isLiked(postId, likedIds);
        isBookmarked = isBookmarked(postId, bookmarkedIds);

        log.info("자료 상세 조회 완료");
        return PostDetailReadRes.from(post, comments, images, isLiked, isBookmarked, likes, bookmarks);
    }

    //해당 유저가, 읽은 게시물 조회
    @Transactional(readOnly = true)
    public Page<PostReadRes> readAllViewedPosts(Pageable pageable){

        //유저 ID를 가지고 해당 유저가 좋아요, 즐겨 찾기 한 게시물의 ID를 SET으로 저장 (쿼리 2)
        //이후에 조회한 게시물과 ID가 같은지 비교해서 누른지 boolean으로 반환
        User user = securityUtil.getCurrentUser();
        Set<Long> likedIds = likedPostIds(user.getId());
        Set<Long> bookmarkedIds = bookmarkedPostIds(user.getId());

        //유저가 읽은 게시물 반환
        return postViewRepository.findAllViewedPostsByUserId(pageable, user.getId())
                .map(post -> {
                    return PostReadRes.from(post, getFirstImageUrl(post), isLiked(post.getId(), likedIds), isBookmarked(post.getId(), bookmarkedIds));
                });
    }

    //해당 유저가, 최근에 읽은 게시물 조회
    @Transactional(readOnly = true)
    public Page<PostReadRes> readAllViewedPostsSortRecentViewed(Pageable pageable){

        //유저 ID를 가지고 해당 유저가 좋아요, 즐겨 찾기 한 게시물의 ID를 SET으로 저장 (쿼리 2)
        //이후에 조회한 게시물과 ID가 같은지 비교해서 누른지 boolean으로 반환
        User user = securityUtil.getCurrentUser();
        Set<Long> likedIds = likedPostIds(user.getId());
        Set<Long> bookmarkedIds = bookmarkedPostIds(user.getId());

        //최근에 읽은 순으로 게시물 반환
        return postViewRepository.findAllPostsByUserIdSortRecentViewed(pageable, user.getId())
                .map(post -> {
                    return PostReadRes.from(post, getFirstImageUrl(post), isLiked(post.getId(), likedIds), isBookmarked(post.getId(), bookmarkedIds));
                });
    }

    //좋아요가 많은 순으로 게시물 조회
    @Transactional(readOnly = true)
    public Page<PostReadRes> readPopularPosts(Pageable pageable) {

        //유저 ID를 가지고 해당 유저가 좋아요, 즐겨 찾기 한 게시물의 ID를 SET으로 저장 (쿼리 2)
        //이후에 조회한 게시물과 ID가 같은지 비교해서 누른지 boolean으로 반환
        User user = securityUtil.getCurrentUser();
        Set<Long> likedIds = likedPostIds(user.getId());
        Set<Long> bookmarkedIds = bookmarkedPostIds(user.getId());

        //좋아요가 많은 게시물 반환
        return postRepository.findPopularPosts(pageable)
                .map(post -> {
                    return PostReadRes.from(post, getFirstImageUrl(post), isLiked(post.getId(), likedIds), isBookmarked(post.getId(), bookmarkedIds));
                });
    }

    //해당 유저가, 작성한 게시물 조회
    @Transactional(readOnly = true)
    public Page<PostReadRes> readAllWrittenPosts(Pageable pageable){

        //유저 ID를 가지고 해당 유저가 좋아요, 즐겨 찾기 한 게시물의 ID를 SET으로 저장 (쿼리 2)
        //이후에 조회한 게시물과 ID가 같은지 비교해서 누른지 boolean으로 반환
        User user = securityUtil.getCurrentUser();
        Set<Long> likedIds = likedPostIds(user.getId());
        Set<Long> bookmarkedIds = bookmarkedPostIds(user.getId());

        //유저가 작성한 게시물 반환
        return postRepository.findPostsByUserId(user.getId(), pageable)
                .map(post -> {
                    return PostReadRes.from(post, getFirstImageUrl(post), isLiked(post.getId(), likedIds), isBookmarked(post.getId(), bookmarkedIds));
                });
    }

    //해당 유저가, 좋아요를 누른 게시물 조회
    @Transactional(readOnly = true)
    public Page<PostReadRes> readAllLikedPosts(Pageable pageable){

        //북마크만 조회
        User user = securityUtil.getCurrentUser();
        Set<Long> bookmarkedIds = bookmarkedPostIds(user.getId());

        //좋아요를 누른 게시물 반홥
        return postLikeRepository.findLikedPostsByUserId(user.getId(), pageable)
                .map(post -> {
                    return PostReadRes.from(post, getFirstImageUrl(post), true, isBookmarked(post.getId(), bookmarkedIds));
                });
    }

    //해당 기간 동안, 생성된 게시물 반환
    @Transactional(readOnly = true)
    public Page<PostReadRes> readAllPostsInDuration(Instant from, Instant to, Pageable pageable){

        //디자인 쪽에서 북마크, 좋아요 표시 안 함
        //기간의 게시물 반환
        return postRepository.findAllPostsByCreateAtInDuration(from, to, pageable)
                .map(post -> {
                    return PostReadRes.from(post, getFirstImageUrl(post), false, false);
                });
    }

    //해당 유저가, 북마크한 게시물만 조회
    @Transactional(readOnly = true)
    public Page<PostReadRes> readAllBookMarkedPosts(Pageable pageable){

        //좋아요만 조회
        User user = securityUtil.getCurrentUser();
        Set<Long> likedIds = likedPostIds(user.getId());

        //북마크된 게시물 반환
        return postBookmarkRepository.findBookmarkedPosts(user.getId(), pageable).map(
                post -> {
                    return PostReadRes.from(post, getFirstImageUrl(post), isLiked(post.getId(), likedIds), true);
                }
        );
    }

    //유저가 게시물의 좋아요 첨삭하게 하는 기능
    @Transactional
    public void toggleLike(Long postId) {
        User user = securityUtil.getCurrentUser();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("좋아요할, 해당 게시글 없음"));

        Optional<PostLike> postLikeOptional = postLikeRepository.findByPostIdAndUserId(postId, user.getId());

        if (postLikeOptional.isPresent()) {
            postLikeRepository.delete(postLikeOptional.get());
            postLikeRepository.minusLikes(postId);
        } else {
            PostLike newLike = new PostLike(post, user);
            postLikeRepository.save(newLike);
            postLikeRepository.addLikes(postId);
        }
    }

    //유저가 게시물의 북마크 첨삭하게 하는 기능
    @Transactional
    public void toggleBookmark(Long postId) {
        User user = securityUtil.getCurrentUser();
        Post post = postRepository.findByIdWithLock(postId)
                .orElseThrow(() -> new PostNotFoundException("즐겨착기할, 해당 게시글 없음"));

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

    //유저 ID를 가지고 좋아요 게시물 ID들 SET 으로 반환
    Set<Long> likedPostIds(Long userId) {
        return postLikeRepository.findLikedPostIds(userId);
    }

    //게시물 ID에, 해당 게시물의 ID가 있는 지 판단
    boolean isLiked(Long post_id, Set<Long> likedPostIds) {
        return likedPostIds.contains(post_id);
    }

    //유저 ID를 가지고 북마크 게시물 ID들 SET 으로 반환
    Set<Long> bookmarkedPostIds(Long userId) {
        return postBookmarkRepository.findBookmarkedPostIds(userId);
    }
    //게시물 ID에, 해당 게시물의 ID가 있는 지 판단
    boolean isBookmarked(Long post_id, Set<Long> bookmarkedPostIds) {
        return bookmarkedPostIds.contains(post_id);
    }


    //사용자 전용, 게시물 신고 접수
    @Transactional
    public void createReport(PostReportReq postReportReq, Long post_id) {

        //신고 게시물, 신고자 조회
        Post post = postRepository.findPostById(post_id)
                .orElseThrow(() -> new PostNotFoundException("신고할 게시글 없음"));
        User user = securityUtil.getCurrentUser();

        //신고 접수 생성
        PostReport postReport = PostReport.builder()
                .description(postReportReq.description())
                .post(post)
                .user(user)
                .build();

        //해당 게시물에 신고 접수 추가
        post.getPostReports().add(postReport);

        postReportRepository.save(postReport);

    }

    //신고자 ID를 가지고 신고 접수 조회
    @Transactional(readOnly = true)
    public Page<PostReportRes> readReport(Pageable pageable) {

        User user = securityUtil.getCurrentUser();

        //신고자의 신고 목록 조회
        Page<PostReport> postReport = postReportRepository.findPostReportByUserId(user.getId(), pageable);

        //신고 목록 반환
        return postReport.map(pr -> {
            return PostReportRes.builder()
                    .post_id(pr.getPost().getId())
                    .description(pr.getDescription())
                    .build();
        });
    }


    //신고 접수만 삭제
    @Transactional
    public void deleteReport(Long report_id) {

        //report_id로 해당하는 게시물을 찾음
        Post post = postReportRepository.findReportPostByPost_id(report_id)
                .orElseThrow(() -> new PostNotFoundException("신고 게시물 목록에 해당 게시물이 없음"));

        //게시물의 일치하는 PostReport를 찾고 해당하는 PostReport를 삭제
        PostReport postReport = post.getPostReports().stream()
                .filter(pr -> pr.getReport_id().equals(report_id))
                .findFirst()
                .orElse(null);
        post.getPostReports().remove(postReport);
    }

    //신고 접수 ID를 통해 해당 게시물 삭제 처리
    @Transactional
    public void deleteReportPost(Long report_id) {
        Post post = postReportRepository.findReportPostByPost_id(report_id)
                .orElseThrow(() -> new PostNotFoundException("신고 게시물 목록에 해당 게시물이 없음"));
        deletePost(post.getId());
    }

    private void processTags(Post post, List<String> tagNames) {
        // 1. 입력받은 태그 이름들로 기존 DB에 존재하는 태그들 한 번에 조회
        List<Tag> existingTags = tagRepository.findAllByNameIn(tagNames);
        Map<String, Tag> tagMap = existingTags.stream()
                .collect(Collectors.toMap(Tag::getName, t -> t));

        // 2. PostTag 생성
        List<PostTag> postTags = tagNames.stream().map(name -> {
            Tag tag = tagMap.get(name);
            if (tag == null) {
                // DB에 없으면 새로 생성 후 저장
                tag = tagRepository.save(new Tag(name));
            }
            return new PostTag(post, tag);
        }).toList();

        // 3. Post 엔티티의 태그 리스트 교체
        post.getPostTags().clear();
        post.getPostTags().addAll(postTags);
    }

    /**
     * 이미지 업로드 로직
     */
    private void uploadImages(Post post, List<MultipartFile> files) {
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            String imageKey = s3Service.upload(file, "posts");
            Image image = Image.builder()
                    .imageKey(imageKey)
                    .imageName(file.getOriginalFilename())
                    .build();

            post.addImage(image); // 연관관계 편의 메서드 호출
        }
    }

}
