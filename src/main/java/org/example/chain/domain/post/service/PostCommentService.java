package org.example.chain.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.example.chain.domain.post.data.req.PostCommentCreateReq;
import org.example.chain.domain.post.data.res.PostCommentReadRes;
import org.example.chain.domain.post.entity.Post;
import org.example.chain.domain.post.entity.PostComment;
import org.example.chain.domain.post.repository.PostCommentRepository;
import org.example.chain.domain.post.repository.PostRepository;
import org.example.chain.global.error.exception.PostNotFoundException;
import org.example.chain.global.security.util.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class PostCommentService {

    private final PostCommentRepository commentRepository;
    private final PostRepository postRepository;
    private final SecurityUtil securityUtil;

    //해당 게시물에 댓글 생성
    @Transactional
    public void createComment(Long postId, Long userId, PostCommentCreateReq request) {

        //게시물 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("해당 게시글이 존재하지 않습니다."));

        //댓글 생성
        PostComment comment = PostComment.builder()
                .post(post)
                .user(securityUtil.getCurrentUser())
                .content(request.content())
                .build();

        post.getPostComments().add(comment);
        commentRepository.save(comment);
    }

    //
    @Transactional(readOnly = true)
    public List<PostCommentReadRes> readComments(Long postId) {

        // 게시글 존재 여부 검증 (UX + 안정성)
        postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("해당 게시글이 존재하지 않습니다."));

        //해당 게시물의 댓글 목록 반환
        return commentRepository.findAllByPostId(postId).stream()
                .map(PostCommentReadRes::from)
                .toList();
    }
}
