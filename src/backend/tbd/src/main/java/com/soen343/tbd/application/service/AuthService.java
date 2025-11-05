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


    public void registerUser(String fullName, String email, String password, String address, String username,
                            String cardHolderName, String cardNumber, String expiryMonth, String expiryYear, String cvc) {
        // Create a new user entity
        User newUser = new Rider(null, fullName, email, password, address, username, new Timestamp(System.currentTimeMillis()), 
                        null);

        // Hash the password before saving
        String encodedPassword = passwordEncoder.encode(password);
        newUser.setPassword(encodedPassword);

        // Set payment information
        newUser.setCardHolderName(cardHolderName);
        newUser.setCardNumber(cardNumber);
        newUser.setExpiryMonth(expiryMonth);
        newUser.setExpiryYear(expiryYear);
        newUser.setCvc(cvc);

        // Save the user to the database
        userRepository.save(newUser);
    }
}
