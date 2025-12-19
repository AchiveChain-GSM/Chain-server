package org.example.chain.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.chain.domain.user.data.request.UpdateReq;
import org.example.chain.domain.user.data.request.SignUpReq;
import org.example.chain.domain.user.data.response.UserRes;
import org.example.chain.domain.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<HttpStatus> createUser (@RequestBody SignUpReq request){
        userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserRes> readUser (@PathVariable("id") Long id){
        return new ResponseEntity<>(userService.readUser(id), HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<HttpStatus> updateUser(@RequestBody UpdateReq request){
        userService.updateUser(request);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser (@PathVariable("id") Long id){
        userService.deleteUser(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
