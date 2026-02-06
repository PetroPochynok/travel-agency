package com.epam.finaltask.service;

import com.epam.finaltask.dto.RegisterRequest;
import com.epam.finaltask.exception.EmailAlreadyExistsException;
import com.epam.finaltask.exception.PasswordsDoNotMatchException;
import com.epam.finaltask.exception.UsernameAlreadyExistsException;
import com.epam.finaltask.model.Role;
import com.epam.finaltask.model.User;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.security.JwtUtil;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @Order(1)
    void register_success() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("customer");
        request.setPassword("12345");
        request.setConfirmPassword("12345");
        request.setFirstName("john");
        request.setLastName("doe");
        request.setEmail("customer@example.com");
        request.setPhoneNumber("+380123456789");

        when(userRepository.existsByUsername("customer")).thenReturn(false);
        when(userRepository.existsByEmail("customer@example.com")).thenReturn(false);

        authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userDetailsService).saveUser(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertNotNull(savedUser);
        assertEquals("customer", savedUser.getUsername());
        assertEquals("John", savedUser.getFirstName());
        assertEquals("Doe", savedUser.getLastName());
        assertEquals("customer@example.com", savedUser.getEmail());
        assertEquals("12345", savedUser.getPassword());
        assertEquals(Role.CUSTOMER, savedUser.getRole());
        assertEquals(BigDecimal.ZERO, savedUser.getBalance());
        assertTrue(savedUser.isActive());
    }

    @Test
    @Order(2)
    void register_usernameAlreadyExists_throwException() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("customer");
        request.setPassword("12345");
        request.setConfirmPassword("12345");
        request.setEmail("customer@example.com");

        when(userRepository.existsByUsername("customer")).thenReturn(true);

        assertThrows(
                UsernameAlreadyExistsException.class,
                () -> authService.register(request)
        );

        verify(userDetailsService, never()).saveUser(any(User.class));
    }

    @Test
    @Order(3)
    void register_passwordsDoNotMatch_throwException() {
        RegisterRequest request = new RegisterRequest();
        request.setPassword("12345");
        request.setConfirmPassword("54321");

        assertThrows(
                PasswordsDoNotMatchException.class,
                () -> authService.register(request)
        );

        verify(userDetailsService, never()).saveUser(any(User.class));
    }

    @Test
    @Order(4)
    void register_emailAlreadyExists_throwException() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("customer");
        request.setPassword("12345");
        request.setConfirmPassword("12345");
        request.setEmail("customer@example.com");

        when(userRepository.existsByUsername("customer")).thenReturn(false);
        when(userRepository.existsByEmail("customer@example.com")).thenReturn(true);

        assertThrows(
                EmailAlreadyExistsException.class,
                () -> authService.register(request)
        );

        verify(userDetailsService, never()).saveUser(any(User.class));
    }
}