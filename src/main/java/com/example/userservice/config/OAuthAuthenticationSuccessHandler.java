package com.example.userservice.config;

import com.example.userservice.dto.response.JwtResponse;
import com.example.userservice.dto.response.MessageResponse;
import com.example.userservice.model.*;
import com.example.userservice.repository.RoleRepository;
import com.example.userservice.service.FirebaseService;
import com.example.userservice.service.UserService;
import com.example.userservice.util.ImageUtils;
import com.example.userservice.util.JwtUtils;
import com.example.userservice.util.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class OAuthAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Autowired
    private ImageUtils imageUtils;
    @Autowired
    private FirebaseService firebaseService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauth2User = token.getPrincipal();

        // Now you can access the OAuth2User's attributes
        Map<String, Object> attributes = oauth2User.getAttributes();
        System.out.println(attributes);
        // Your custom logic here
        //if user does not exist register it
        if (!userService.existsByEmail((String) attributes.get("email"))) {
            BufferedImage picture = imageUtils.downloadImage((String) attributes.get("picture"));
            String photoName = firebaseService.uploadBufferedImage(picture, "jpg", "image/jpeg");
            User user = User.builder()
                    .username((String) attributes.get("name"))
                    .firstname((String) attributes.get("given_name"))
                    .lastname((String) attributes.get("family_name"))
                    .password(passwordEncoder.encode((String) attributes.get("sub")))
                    .email((String) attributes.get("email"))
                    .emailConfirmed(true)
                    .roles(Set.of(roleRepository.findByName(ERole.ROLE_USER).orElseThrow()))
                    .provider(Provider.GOOGLE)
                    .imgName(photoName)
                    .build();
            userService.save(user);

            mapper.writeValue(response.getOutputStream(), new MessageResponse("User registered successfully!"));
        } else {
            User byUsername = userService.get((String) attributes.get("name"));

            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(attributes.get("name"), attributes.get("sub")));

            SecurityContextHolder.getContext().setAuthentication(auth);
            String jwt = jwtUtils.generateJwtToken(auth);

            UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            mapper.writeValue(response.getOutputStream(), new JwtResponse(jwt, roles, userMapper.map(byUsername)));
        }
//        response.sendRedirect("/my-custom-url");
    }
}
