package org.example.chain.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.chain.domain.auth.service.EmailService;
import org.example.chain.domain.user.data.request.SignUpReq;
import org.example.chain.domain.user.data.request.UpdateReq;
import org.example.chain.domain.user.data.response.UserRes;
import org.example.chain.domain.user.entity.CustomUserDetails;
import org.example.chain.domain.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.example.chain.domain.user.entity.User;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public void createUser(SignUpReq request){
        User user = new User(request, passwordEncoder.encode(request.password()));

        String token = emailService.createVerificationToken(user);
        emailService.sendVerificationEmail(user.getEmail(), token);

        userRepository.save(user);
    }

    @Transactional
    public UserRes readUser(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 아이디의 사용자를 찾을 수 없음"));
        return new UserRes(user.getEmail());
    }

    @Transactional
    public CustomUserDetails readUser(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자를 찾을 수 없음"));
        return new CustomUserDetails(user);
    }

    @Transactional
    public void updateUser(UpdateReq request){
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자를 찾을 수 없음"));
        user.update(request, passwordEncoder.encode(request.password()));
    }

    @Transactional
    public void deleteUser(Long postReport_id){
        User user = userRepository.findById(postReport_id)
                .orElseThrow(() -> new IllegalArgumentException("해당 아이디의 사용자를 찾을 수 없음"));
        userRepository.delete(user);
    }
}
