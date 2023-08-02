package com.example.userservice.util;

import com.example.userservice.dto.response.UserProfileResponse;
import com.example.userservice.model.User;
import com.example.userservice.service.FirebaseService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;

@Component
@AllArgsConstructor
public class UserMapper {
    private FirebaseService firebaseService;

    public UserProfileResponse map(User user) throws IOException {
        URL url = firebaseService.generateURL(user.getImgName());
        UserProfileResponse resp = new UserProfileResponse(user);
        resp.setImageUrl(url);
        return resp;
    }
}
