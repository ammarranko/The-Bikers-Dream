package com.soen343.tbd.application.service;

import com.soen343.tbd.infrastructure.persistence.entity.User;
import com.soen343.tbd.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Optional;

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
        User newUser = new User();
        newUser.setFullName(fullName);
        newUser.setEmail(email);
        newUser.setAddress(address);
        newUser.setUsername(username);
        // Set default role
        newUser.setRole("Rider");
        // Set created_at timestamp
        newUser.setCreated_at(new Timestamp(System.currentTimeMillis()));
        // Hash the password before saving
        String encodedPassword = passwordEncoder.encode(password);
        newUser.setPassword(encodedPassword);

        // Save the user to the database
        userRepository.save(newUser);
    }
}
