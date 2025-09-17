package com.desarrollo.raffy.presenter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.desarrollo.raffy.dto.AuthResponse;
import com.desarrollo.raffy.dto.ChangePasswordRequest;
import com.desarrollo.raffy.dto.LoginRequest;
import com.desarrollo.raffy.dto.RefreshTokenRequest;
import com.desarrollo.raffy.dto.RegisteredUserDTO;
import com.desarrollo.raffy.dto.UpdateProfileRequest;
import com.desarrollo.raffy.dto.UserResponse;
import com.desarrollo.raffy.business.services.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisteredUserDTO request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            AuthResponse response = authService.refreshToken(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        try {
            UserResponse user = authService.getCurrentUser();
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            authService.logout(request.getRefreshToken());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        try {
            UserResponse updatedUser = authService.updateProfile(request);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        try {
            authService.changePassword(request);
            return ResponseEntity.ok().body("Contraseña actualizada correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/users/{nickname}")
    public ResponseEntity<UserResponse> getUserByNickname(@PathVariable String nickname) {
        try {
            UserResponse user = authService.getUserByNickname(nickname);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}