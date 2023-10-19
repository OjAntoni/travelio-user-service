package com.example.userservice.controller;

import com.example.userservice.dto.request.UserUpdateRequest;
import com.example.userservice.model.UserDetailsImpl;
import com.example.userservice.service.QueueService;
import com.example.userservice.util.JwtUtils;
import com.example.userservice.dto.response.UserProfileResponse;
import com.example.userservice.model.User;
import com.example.userservice.service.FirebaseService;
import com.example.userservice.service.UserService;
import com.example.userservice.util.UserMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.Valid;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.BindingResult;
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
    @Autowired
    private QueueService queueService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserDetailsService userDetailsService;

    @PostMapping("/profile/image")
    public ResponseEntity<UserProfileResponse> uploadImage(@RequestHeader("Authorization") String jwt, MultipartFile image) throws IOException {
        String username = jwtUtils.getUserNameFromJwtToken(jwt.substring(7));
        User user = userService.get(username);
        String img = firebaseService.uploadFile(image);
        user.setImgName(img);
        User updatedUser = userService.update(user);
        queueService.sendToTopic("users_event", objectMapper.writeValueAsString(updatedUser), "USER_UPDATED");
        return new ResponseEntity<>(userMapper.map(updatedUser), HttpStatus.OK);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getOwnUserProfile(Principal principal) throws IOException {
        String name = principal.getName();
        User user = userService.get(name);
        return new ResponseEntity<>(userMapper.map(user), HttpStatus.OK);
    }

    @GetMapping("/profile/{username}")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable("username") String name) throws IOException {
        User user = userService.get(name);
        return new ResponseEntity<>(userMapper.map(user), HttpStatus.OK);
    }

    @PatchMapping("/profile")
    @SneakyThrows
    public ResponseEntity<?> updateUserProfile(@Valid @RequestBody UserUpdateRequest uur, Principal principal){
        User oldUser = userService.get(principal.getName());
        User updatedUser = userService.updateUser(oldUser.getId(), uur);

        queueService.sendToTopic("users_events", objectMapper.writeValueAsString(updatedUser), "USER_UPDATED");

        UserProfileResponse mapped = userMapper.map(updatedUser);
        JsonNode jsonNode = objectMapper.valueToTree(mapped);

        if (!uur.getUsername().equals(oldUser.getUsername())){
            UserDetails userDetails = userDetailsService.loadUserByUsername(updatedUser.getUsername());
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);

            ((ObjectNode) jsonNode).put("token", jwtUtils.generateJwtToken(authentication));
        }
        return new ResponseEntity<>(objectMapper.writeValueAsString(jsonNode), HttpStatus.OK);
    }

}
