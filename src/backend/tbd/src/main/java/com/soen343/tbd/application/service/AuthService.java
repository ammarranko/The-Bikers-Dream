package com.soen343.tbd.application.service;

import com.soen343.tbd.domain.model.user.User;
import com.soen343.tbd.domain.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Optional;

import com.soen343.tbd.domain.model.user.Rider;

@Service
public class AuthService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    public boolean authenticate(String email, String password) {

        // Find user by email in database
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            // User not found
            return false;
        }

        User user = userOptional.get();

        // Check if password matches
        boolean passwordMatches;

        // Check if the stored password looks like it's hashed (starts with $2a, $2b, etc.)
        if (user.getPassword() != null && user.getPassword().startsWith("$2")) {
            passwordMatches = passwordEncoder.matches(password, user.getPassword());
        } else {
            passwordMatches = password.equals(user.getPassword());
        }

        return passwordMatches;
    }

    /**
     * Register a new user with the given details
     *
     * @param fullName user's full name
     * @param email    user's email address
     * @param password user's password (will be encoded)
     */
    public void registerUser(String fullName, String email, String password, String address, String username) {
        // Create a new user entity
        User newUser = new Rider(null, fullName, email, password, address, username, new Timestamp(System.currentTimeMillis()), 
                        null, new ArrayList<>(), new ArrayList<>());

        // Hash the password before saving
        String encodedPassword = passwordEncoder.encode(password);
        newUser.setPassword(encodedPassword);

        // Save the user to the database
        userRepository.save(newUser);
    }
}
