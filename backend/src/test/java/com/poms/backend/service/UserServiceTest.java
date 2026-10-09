package com.poms.backend.service;

import com.poms.backend.entity.User;
import com.poms.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1);
        sampleUser.setFullName("John Doe");
        sampleUser.setEmail("john.doe@example.com");
        sampleUser.setPassword("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy");
        sampleUser.setRole("Admin");
        sampleUser.setStatus("Active");
        sampleUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("findByEmail returns user when user exists")
    void findByEmail_WhenUserExists_ReturnsUser() {
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(sampleUser));

        Optional<User> result = userService.findByEmail("john.doe@example.com");

        assertTrue(result.isPresent());
        assertEquals("john.doe@example.com", result.get().getEmail());
        assertEquals("John Doe", result.get().getFullName());
        assertEquals("Admin", result.get().getRole());
        verify(userRepository, times(1)).findByEmail("john.doe@example.com");
    }

    @Test
    @DisplayName("findByEmail returns empty Optional when user does not exist")
    void findByEmail_WhenUserDoesNotExist_ReturnsEmpty() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        Optional<User> result = userService.findByEmail("unknown@example.com");

        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByEmail("unknown@example.com");
    }

    @Test
    @DisplayName("findById returns user when user exists")
    void findById_WhenUserExists_ReturnsUser() {
        when(userRepository.findById(1)).thenReturn(Optional.of(sampleUser));

        Optional<User> result = userService.findById(1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
        assertEquals("John Doe", result.get().getFullName());
        verify(userRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("findById returns empty Optional when user does not exist")
    void findById_WhenUserDoesNotExist_ReturnsEmpty() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        Optional<User> result = userService.findById(999);

        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findById(999);
    }
}
