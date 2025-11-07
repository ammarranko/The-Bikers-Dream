package com.soen343.tbd.application.service;

import com.soen343.tbd.domain.model.user.User;
import com.soen343.tbd.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Service to easily retrieve the currently authenticated user from the security context.
 * This service extracts the user's email from the JWT token (stored in SecurityContext)
 * and fetches the full User object from the database.
 */
@Service
public class CurrentUserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Gets the email of the currently authenticated user from the JWT token.
     */
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof String) {
            return (String) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * Gets the full User object of the currently authenticated user.
     */
    public User getCurrentUser() {
        String email = getCurrentUserEmail();
        if (email != null) {
            return userRepository.findByEmail(email).orElse(null);
        }
        return null;
    }

    /**
     * Checks if there is a currently authenticated user.
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof String;
    }
}

