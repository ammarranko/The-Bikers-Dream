package com.soen343.tbd.application.service;

import com.soen343.tbd.domain.model.user.Rider;
import com.soen343.tbd.domain.model.user.User;
import com.soen343.tbd.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Timestamp;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

@Mock
private UserRepository userRepository;

@Mock
private PasswordEncoder passwordEncoder;

@InjectMocks
private AuthService authService;

private User user;

@BeforeEach
void setUp() {
    user = new Rider(
            null,
            "John Doe",
            "john@example.com",
            "$2a$10$hashedpassword", // pretend hashed password
            "123 Street",
            "johndoe",
            new Timestamp(System.currentTimeMillis()),
            null,
            null
    );
}

@Test
void testAuthenticateSuccessWithHashedPassword() {
    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("password123", user.getPassword())).thenReturn(true);

    boolean result = authService.authenticate("john@example.com", "password123");
    System.out.println("[SUCCESS] Authenticate with hashed password result: " + result);

    assertTrue(result);
    verify(userRepository).findByEmail("john@example.com");
    verify(passwordEncoder).matches("password123", user.getPassword());
}

@Test
void testAuthenticateSuccessWithPlainPassword() {
    user.setPassword("plainpassword"); // simulate un-hashed password
    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

    boolean result = authService.authenticate("john@example.com", "plainpassword");
    System.out.println("[SUCCESS] Authenticate with plain password result: " + result);

    assertTrue(result);
    verify(userRepository).findByEmail("john@example.com");
    verifyNoInteractions(passwordEncoder); // no hash check for plain password
}

@Test
void testAuthenticateFailWrongPassword() {
    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrongpass", user.getPassword())).thenReturn(false);

    boolean result = authService.authenticate("john@example.com", "wrongpass");
    System.out.println("[FAIL] Authenticate with wrong password result: " + result);

    assertFalse(result);
    verify(userRepository).findByEmail("john@example.com");
    verify(passwordEncoder).matches("wrongpass", user.getPassword());
}

@Test
void testAuthenticateFailUserNotFound() {
    when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

    boolean result = authService.authenticate("unknown@example.com", "password");
    System.out.println("[FAIL] Authenticate with non-existent user result: " + result);

    assertFalse(result);
    verify(userRepository).findByEmail("unknown@example.com");
    verifyNoInteractions(passwordEncoder);
}

@Test
void testRegisterUser() {
    when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

    authService.registerUser(
            "John Doe",
            "john@example.com",
            "password123",
            "123 Street",
            "johndoe",
            "John Doe",
            "4111111111111111",
            "12",
            "2025",
            "123"
    );

    System.out.println("[SUCCESS] Register user executed for johndoe");

    // Verify save was called
    verify(userRepository, times(1)).save(any(Rider.class));
    // Verify password encoding
    verify(passwordEncoder, times(1)).encode("password123");
}


}
