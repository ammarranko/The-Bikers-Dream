package com.soen343.tbd.application.service;

import com.soen343.tbd.domain.model.ids.UserId;
import com.soen343.tbd.domain.model.user.User;
import com.soen343.tbd.domain.repository.UserRepository;
import com.soen343.tbd.application.dto.LoginRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// a class cotnainting the services to offer with a user like saving a user to the db
@Service
public class UserService {
    @Autowired
    UserRepository userRepository;

    public void addUser(User user) {
        userRepository.save(user);
    }

    public Boolean loginUser(LoginRequest loginRequest) {
        for (User user : userRepository.findAll()) {
            if (user.getEmail().equals(loginRequest.getEmail())
                    && user.getPassword().equals(loginRequest.getPassword())) {
                return true;
            }
        }
        return false;
    }

    public User getUserWithEmail(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("No user found with email: " + userEmail));
    }

    public String getUserEmail(UserId userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId.value()));

            return user.getEmail();
        } catch (Exception e) {
            return "Unknown User";
        }
    }

    public User getUserById(UserId userId){
        return userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("No user found with id: " + userId.value()));
    }
}
