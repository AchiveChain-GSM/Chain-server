package org.example.chain.domain.post.entity;

import jakarta.persistence.*;
import lombok.*;
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
@Setter
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

    @Builder.Default
    @ColumnDefault("0")
    @Column(name = "likes", nullable = false)
    private Long likes = 0L;

    @Builder.Default
    @ColumnDefault("0")
    @Column(name = "bookmarks", nullable = false)
    private Long bookmarks = 0L;

    @Builder.Default
    @BatchSize(size = 100) // postTags를 조회할 때 최대 100개씩 IN 쿼리로 묶어서 가져옴
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PostTag> postTags = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @BatchSize(size = 100)
    private User user;

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<PostLike> postLikes = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @BatchSize(size = 100)
    private List<PostComment> postComments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<PostBookmark> postBookmark = new ArrayList<>();

    @Builder.Default
    @ColumnDefault("0")
    @Column(name = "views", nullable = false)
    private Long views = 0L;

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<PostView> postViews = new ArrayList<>();

    @Builder.Default
    @BatchSize(size = 100)
    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Image> images = new HashSet<>();

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
