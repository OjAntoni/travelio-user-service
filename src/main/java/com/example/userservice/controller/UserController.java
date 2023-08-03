package com.example.userservice.controller;

import com.example.userservice.util.JwtUtils;
import com.example.userservice.dto.response.UserProfileResponse;
import com.example.userservice.model.User;
import com.example.userservice.service.FirebaseService;
import com.example.userservice.service.UserService;
import com.example.userservice.util.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private FirebaseService firebaseService;
    @Autowired
    private UserMapper userMapper;

    @PostMapping("/profile/image")
    public ResponseEntity<UserProfileResponse> uploadImage(@RequestHeader("Authorization") String jwt, MultipartFile image) throws IOException {
        String username = jwtUtils.getUserNameFromJwtToken(jwt.substring(7));
        User user = userService.get(username);
        String img = firebaseService.uploadFile(image);
        user.setImgName(img);
        return new ResponseEntity<>(userMapper.map(userService.update(user)), HttpStatus.OK);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(Principal principal) throws IOException {
        String name = principal.getName();
        User user = userService.get(name);
        return new ResponseEntity<>(userMapper.map(user), HttpStatus.OK);
    }
}
