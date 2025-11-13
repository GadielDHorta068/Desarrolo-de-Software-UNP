/**
 * Pruebas unitarias para UserManagementController.
 */
package com.argy.twofactorauth.controller;

import com.argy.twofactorauth.dto.CreateUserRequest;
import com.argy.twofactorauth.dto.UserResponse;
import com.argy.twofactorauth.dto.TwoFactorStatusResponse;
import com.argy.twofactorauth.dto.SystemStatsResponse;
import com.argy.twofactorauth.entity.User;
import com.argy.twofactorauth.entity.RecoveryCode;
import com.argy.twofactorauth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserManagementControllerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserManagementController controller;

    @BeforeEach
    void setUp() {
        // MockitoAnnotations.openMocks(this); // No longer needed with @ExtendWith
    }

    @Test
    void getTwoFactorStatus_UserExists_ReturnsStatus() {
        // Arrange
        String username = "testuser";
        User user = new User();
        user.setUsername(username);
        user.setSecret("encrypted_secret");
        
        RecoveryCode code1 = new RecoveryCode();
        code1.setUsed(false);
        RecoveryCode code2 = new RecoveryCode();
        code2.setUsed(true);
        user.setRecoveryCodes(Arrays.asList(code1, code2));
        
        when(userRepository.findByUsername(username)).thenReturn(user);
        
        // Act
        ResponseEntity<TwoFactorStatusResponse> response = controller.getTwoFactorStatus(username);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(username, response.getBody().getUsername());
        assertTrue(response.getBody().isTwoFactorEnabled());
        assertEquals(1, response.getBody().getRemainingRecoveryCodes());
    }

    @Test
    void getTwoFactorStatus_UserNotFound_ReturnsNotFound() {
        // Arrange
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(null);
        
        // Act
        ResponseEntity<TwoFactorStatusResponse> response = controller.getTwoFactorStatus(username);
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getUser_UserExists_ReturnsUserInfo() {
        // Arrange
        String username = "testuser";
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setSecret("encrypted_secret");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        when(userRepository.findByUsername(username)).thenReturn(user);
        
        // Act
        ResponseEntity<UserResponse> response = controller.getUser(username);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(username, response.getBody().getUsername());
        assertTrue(response.getBody().isTwoFactorEnabled());
    }

    @Test
    void createUser_NewUser_ReturnsCreated() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("newuser");
        
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("newuser");
        savedUser.setCreatedAt(LocalDateTime.now());
        savedUser.setUpdatedAt(LocalDateTime.now());
        
        when(userRepository.findByUsername("newuser")).thenReturn(null);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        // Act
        ResponseEntity<UserResponse> response = controller.createUser(request);
        
        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("newuser", response.getBody().getUsername());
        assertFalse(response.getBody().isTwoFactorEnabled());
    }

    @Test
    void createUser_UserExists_ReturnsConflict() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("existinguser");
        
        User existingUser = new User();
        existingUser.setUsername("existinguser");
        
        when(userRepository.findByUsername("existinguser")).thenReturn(existingUser);
        
        // Act
        ResponseEntity<UserResponse> response = controller.createUser(request);
        
        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void deleteUser_UserExists_ReturnsNoContent() {
        // Arrange
        String username = "testuser";
        User user = new User();
        user.setUsername(username);
        
        when(userRepository.findByUsername(username)).thenReturn(user);
        
        // Act
        ResponseEntity<Void> response = controller.deleteUser(username);
        
        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void deleteUser_UserNotFound_ReturnsNotFound() {
        // Arrange
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(null);
        
        // Act
        ResponseEntity<Void> response = controller.deleteUser(username);
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userRepository, never()).delete(any());
    }

    @Test
    void getSystemStats_ReturnsStats() {
        // Arrange
        User user1 = new User();
        user1.setSecret("secret1");
        user1.setRecoveryCodes(Arrays.asList(new RecoveryCode(), new RecoveryCode()));
        
        User user2 = new User();
        user2.setSecret(null);
        
        User user3 = new User();
        user3.setSecret("secret3");
        RecoveryCode usedCode = new RecoveryCode();
        usedCode.setUsed(true);
        user3.setRecoveryCodes(Arrays.asList(usedCode, new RecoveryCode()));
        
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2, user3));
        
        // Act
        ResponseEntity<SystemStatsResponse> response = controller.getSystemStats();
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().getTotalUsers());
        assertEquals(2, response.getBody().getUsersWith2FA());
        assertEquals(4, response.getBody().getTotalRecoveryCodes());
        assertEquals(1, response.getBody().getUsedRecoveryCodes());
        assertEquals(66.67, response.getBody().getTwoFactorAdoptionRate(), 0.01);
    }

    @Test
    void getUsersWith2FA_ReturnsFilteredList() {
        // Arrange
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setSecret("secret1");
        user1.setCreatedAt(LocalDateTime.now());
        user1.setUpdatedAt(LocalDateTime.now());
        
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setSecret(null);
        user2.setCreatedAt(LocalDateTime.now());
        user2.setUpdatedAt(LocalDateTime.now());
        
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));
        
        // Act
        ResponseEntity<List<UserResponse>> response = controller.getUsersWith2FA();
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("user1", response.getBody().get(0).getUsername());
        assertTrue(response.getBody().get(0).isTwoFactorEnabled());
    }
}