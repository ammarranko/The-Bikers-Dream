package com.soen343.tbd.application.service;

import com.soen343.tbd.domain.model.user.Rider;
import com.soen343.tbd.domain.model.user.User;
import com.soen343.tbd.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.sql.Timestamp;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock private UserRepository userRepository;

    @InjectMocks private CurrentUserService currentUserService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new Rider(
                null,
                "John Doe",
                "john@example.com",
                "password123",
                "123 Street",
                "johndoe",
                new Timestamp(System.currentTimeMillis()),
                null,
                null
        );
    }

    @Test
    void testGetCurrentUserEmailAuthenticated() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn("john@example.com");

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);

        try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
            mocked.when(SecurityContextHolder::getContext).thenReturn(context);

            String email = currentUserService.getCurrentUserEmail();
            if (email != null) {
                System.out.println("[SUCCESS] Found current user email: " + email);
            } else {
                System.out.println("[FAIL] No current user email found");
            }
            assertEquals("john@example.com", email);
        }
    }

    @Test
    void testGetCurrentUserEmailNotAuthenticated() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);

        try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
            mocked.when(SecurityContextHolder::getContext).thenReturn(context);

            String email = currentUserService.getCurrentUserEmail();
            if (email != null) {
                System.out.println("[SUCCESS] Found current user email: " + email);
            } else {
                System.out.println("[FAIL] No current user email found");
            }
            assertNull(email);
        }
    }

    @Test
    void testGetCurrentUserFound() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn("john@example.com");

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
            mocked.when(SecurityContextHolder::getContext).thenReturn(context);

            User result = currentUserService.getCurrentUser();
            if (result != null) {
                System.out.println("[SUCCESS] Found current user with email: " + result.getEmail());
            } else {
                System.out.println("[FAIL] No current user found");
            }
            assertEquals("john@example.com", result.getEmail());
        }
    }

    @Test
    void testGetCurrentUserNotFound() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn("unknown@example.com");

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
            mocked.when(SecurityContextHolder::getContext).thenReturn(context);

            User result = currentUserService.getCurrentUser();
            if (result != null) {
                System.out.println("[SUCCESS] Found current user with email: " + result.getEmail());
            } else {
                System.out.println("[FAIL] No current user found");
            }
            assertNull(result);
        }
    }

    @Test
    void testIsAuthenticatedTrue() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn("john@example.com");

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);

        try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
            mocked.when(SecurityContextHolder::getContext).thenReturn(context);

            boolean authResult = currentUserService.isAuthenticated();
            if (authResult) {
                System.out.println("[SUCCESS] User is authenticated");
            } else {
                System.out.println("[FAIL] User is not authenticated");
            }
            assertTrue(authResult);
        }
    }

    @Test
    void testIsAuthenticatedFalse() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);

        try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
            mocked.when(SecurityContextHolder::getContext).thenReturn(context);

            boolean authResult = currentUserService.isAuthenticated();
            if (authResult) {
                System.out.println("[SUCCESS] User is authenticated");
            } else {
                System.out.println("[FAIL] User is not authenticated");
            }
            assertFalse(authResult);
        }
    }
}
