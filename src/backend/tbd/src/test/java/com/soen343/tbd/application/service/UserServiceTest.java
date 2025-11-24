package com.soen343.tbd.application.service;

import com.soen343.tbd.application.dto.LoginRequest;
import com.soen343.tbd.domain.model.ids.UserId;
import com.soen343.tbd.domain.model.user.Rider;
import com.soen343.tbd.domain.model.user.User;
import com.soen343.tbd.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;

    @InjectMocks private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new Rider(
                new UserId(1L),
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
    void testLoginUserSuccess() {
        LoginRequest loginRequest = new LoginRequest("john@example.com", "password123");
        when(userRepository.findAll()).thenReturn(Arrays.asList(user));

        Boolean result = userService.loginUser(loginRequest);
        System.out.println(result ? "[SUCCESS] Login success" : "[FAIL] Login failed");

        assertTrue(result);
    }

    @Test
    void testLoginUserFailWrongPassword() {
        LoginRequest loginRequest = new LoginRequest("john@example.com", "wrongpass");
        when(userRepository.findAll()).thenReturn(Arrays.asList(user));

        Boolean result = userService.loginUser(loginRequest);
        System.out.println(result ? "[SUCCESS] Login success (unexpected)" : "[FAIL] Wrong password login failed as expected");

        assertFalse(result);
    }

    @Test
    void testLoginUserFailUserNotFound() {
        LoginRequest loginRequest = new LoginRequest("unknown@example.com", "password123");
        when(userRepository.findAll()).thenReturn(Arrays.asList(user));

        Boolean result = userService.loginUser(loginRequest);
        System.out.println(result ? "[SUCCESS] Login success (unexpected)" : "[FAIL] User not found login failed as expected");

        assertFalse(result);
    }

    @Test
    void testGetUserWithEmailSuccess() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        User result = userService.getUserWithEmail("john@example.com");
        System.out.println("[SUCCESS] Found user with email: " + result.getEmail());

        assertEquals(user, result);
    }

    @Test
    void testGetUserWithEmailFail() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.getUserWithEmail("unknown@example.com");
        });
        System.out.println("[FAIL] " + exception.getMessage());

        assertTrue(exception.getMessage().contains("No user found with email"));
    }
}
