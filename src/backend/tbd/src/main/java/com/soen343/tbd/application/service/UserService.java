package com.soen343.tbd.application.service;

import com.soen343.tbd.infrastructure.persistence.entity.User;
import com.soen343.tbd.repository.UserRepository;
import com.soen343.tbd.application.dto.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
// a class cotnainting the services to offer with a user like saving a user to the db
    @Autowired
    UserRepository userRepository;
    public User addUser(User user) {
        return userRepository.save(user);
    }
    public Boolean loginUser(LoginRequest loginRequest){
        for (User user : userRepository.findAll()) {
            if (user.getEmail().equals(loginRequest.getEmail()) && user.getPassword().equals(loginRequest.getPassword())) {
                return true;
            }
        }
        return false;
    }
}
