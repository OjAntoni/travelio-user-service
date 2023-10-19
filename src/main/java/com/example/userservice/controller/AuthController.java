package com.example.userservice.controller;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.userservice.model.*;
import com.example.userservice.service.QueueService;
import com.example.userservice.util.JwtUtils;
import com.example.userservice.dto.request.LoginRequest;
import com.example.userservice.dto.request.SignupRequest;
import com.example.userservice.dto.response.JwtResponse;
import com.example.userservice.dto.response.MessageResponse;
import com.example.userservice.repository.RoleRepository;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.EmailService;
import com.example.userservice.service.UserService;
import com.example.userservice.service.VerificationCodeService;
import com.example.userservice.util.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    EmailService emailService;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    VerificationCodeService codeService;

    @Autowired
    UserService userService;

    @Autowired
    UserMapper userMapper;

    @Autowired
    QueueService queueService;
    @Autowired
    ObjectMapper objectMapper;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) throws IOException {
        User byUsername = userService.get(loginRequest.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

//        if(!byUsername.isEmailConfirmed()){
//            return ResponseEntity.badRequest().body(new MessageResponse("Error: email has not been confirmed"));
//        }


        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt, roles, userMapper.map(byUsername)));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        if (!signUpRequest.getPassword().equals(signUpRequest.getPasswordRepeated())){
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Passwords mismatch!"));
        }

        // Create new user's account
        User user = new User(signUpRequest.getFirstname(),
                signUpRequest.getLastname(),
                signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin" -> {
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);
                    }
                    case "mod" -> {
                        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(modRole);
                    }
                    default -> {
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                    }
                }
            });
        }

        user.setRoles(roles);

        String imgTitle = userService.getRandomProfilePhotoTitle();
        user.setImgName(imgTitle);
        user.setProvider(Provider.LOCAL);
        userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), codeService.createCode(user.getEmail()).getValue());

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @GetMapping("/verify")
    @SneakyThrows
    public ResponseEntity<?> verifyUserEmail(@RequestParam UUID code){
        User user = codeService.findByCode(code);
        if (codeService.isExpired(code)) {
            return new ResponseEntity<>(new MessageResponse("Code is expired"), HttpStatus.BAD_REQUEST);
        }
        user.setEmailConfirmed(true);
        User saved = userRepository.save(user);
        queueService.sendToTopic("users_events", objectMapper.writeValueAsString(saved), "USER_CREATED");
        return new ResponseEntity<>(HttpStatus.OK);


    }

    @GetMapping("/verify/resend")
    public ResponseEntity<?> resendVerificationEmail(@RequestParam String email){
        User user = userRepository.findByEmail(email);
        if(user==null){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            UUID newCode = codeService.resetCode(user.getEmail());
            emailService.sendVerificationEmail(email,newCode);
            return ResponseEntity.ok().build();
        }
    }


}
