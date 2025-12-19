package org.example.chain.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.chain.domain.post.entity.Post;
import org.example.chain.domain.post.entity.PostLike;
import org.example.chain.domain.post.entity.PostView;
import org.example.chain.domain.user.data.request.SignUpReq;
import org.example.chain.domain.user.data.request.UpdateReq;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "user_name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "Role")
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Post> post = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<PostView> postViews = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<PostLike> postLikes = new ArrayList<>();


    public User(SignUpReq request){
        email = request.email();
        password = request.password();
        roles.add(request.role());
    }

    public void update(UpdateReq request){
        email = request.email();
        password = request.password();
        roles = request.roles();
    }
}
