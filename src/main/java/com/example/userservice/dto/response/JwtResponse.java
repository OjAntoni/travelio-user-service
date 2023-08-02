package com.example.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private List<String> roles;
    private UserProfileResponse user;


    public JwtResponse(String accessToken, List<String> roles, UserProfileResponse user) {
        this.token = accessToken;
        this.user = user;
        this.roles = roles;
    }
}
