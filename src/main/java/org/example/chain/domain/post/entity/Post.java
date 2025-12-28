package org.example.chain.domain.post.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.chain.domain.post.data.req.PostUpdateReq;
import org.example.chain.domain.user.entity.User;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "post")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @CreatedDate
    @Column(name = "create_at", updatable = false)
    private Instant createAt;

    @ColumnDefault("0")
    @Column(name = "likes")
    private Long likes = 0L;

    @ColumnDefault("0")
    @Column(name = "bookmarks")
    private Long bookmarks = 0L;

    @Builder.Default
    @BatchSize(size = 100) // postTags를 조회할 때 최대 100개씩 IN 쿼리로 묶어서 가져옴
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PostTag> postTags = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<PostLike> postLikes = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<PostComment> postComments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<PostBookmark> postBookmark = new ArrayList<>();

    @ColumnDefault("0")
    @Column(name = "views")
    private Long views = 0L;

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<PostView> postViews = new ArrayList<>();

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostReport>  postReports = new ArrayList<>();

    public void updatePost(PostUpdateReq postUpdateReq) {
        this.title = postUpdateReq.title();
        this.content = postUpdateReq.content();
    }

    public void addImage(Image image){
        this.images.add(image);
        if (image.getPost() != this){
            image.setPost(this);
        }
    }

    public void updateTags(List<PostTag> newPostTags) {
        this.postTags.clear();
        this.postTags.addAll(newPostTags);
    }

    // 특정 이미지만 삭제하는 메서드
    public void removeImage(Image image) {
        this.images.remove(image);
        image.setPost(null);
    }
}
