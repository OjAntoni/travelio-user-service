package com.example.userservice.controller;

import com.example.userservice.service.FirebaseService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping()
public class TestController {

    @Autowired
    FirebaseService firebaseService;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @GetMapping("api/test/all")
    public String allAccess() {
        return "Public Content.";
    }

    @GetMapping("api/test/user")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public String userAccess() {
        return "User Content.";
    }

    @GetMapping("api/test/mod")
    @PreAuthorize("hasRole('MODERATOR')")
    public String moderatorAccess() {
        return "Moderator Board.";
    }

    @GetMapping("api/test/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminAccess() {
        return "Admin Board.";
    }

    @PostMapping("api/test/image")
    public ResponseEntity<?> uploadImage(@RequestParam MultipartFile file) throws IOException {
        return new ResponseEntity<>(firebaseService.uploadFile(file), HttpStatus.OK);
    }

    @GetMapping("/my-custom-url")
    public String getLoginPage(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient,
                               @AuthenticationPrincipal OAuth2User oauth2User) {
//        String accessTokenValue = authorizedClient.getAccessToken().getTokenValue();
//        System.out.println(authorizedClient.getAccessToken().getScopes());
//        System.out.println(oauth2User.getAttributes());
//        return accessTokenValue; // return to the login page if the token is null
        return "Hi";
    }
}
