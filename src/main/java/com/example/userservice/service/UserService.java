package com.example.userservice.service;

import com.example.userservice.dto.request.UserUpdateRequest;
import com.example.userservice.exception.EntityNotFoundException;
import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Random;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    private final String DEFAULT_PROFILE_PHOTO_PREFIX="profile_photo_default_";

    public User get(String username){
        User byUsername = userRepository.findByUsername(username);
        if(byUsername==null) throw new EntityNotFoundException("User with username '"+username+"' not found");
        return byUsername;
    }

    public User update(User user){
        if(userRepository.findById(user.getId()).isEmpty())
            throw new EntityNotFoundException("User with id="+user.getId()+" not found");
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, UserUpdateRequest request) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (request.getUsername() != null) {
            existingUser.setUsername(request.getUsername());
        }
        if (request.getFirstname() != null) {
            existingUser.setFirstname(request.getFirstname());
        }
        if (request.getLastname() != null) {
            existingUser.setLastname(request.getLastname());
        }
        // handle other fields similarly

        return userRepository.save(existingUser);
    }

    public String getRandomProfilePhotoTitle(){
        Random r = new Random();
        return DEFAULT_PROFILE_PHOTO_PREFIX+(r.nextInt(8)+1)+".png";
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public boolean existsByEmail(String email){
        return userRepository.existsByEmail(email);
    }
}
