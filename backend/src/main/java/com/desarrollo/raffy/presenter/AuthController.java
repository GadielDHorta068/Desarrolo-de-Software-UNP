package com.desarrollo.raffy.presenter;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
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
import com.desarrollo.raffy.business.repository.RegisteredUserRepository;
import com.desarrollo.raffy.model.RegisteredUser;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación", description = "Endpoints para registro, inicio de sesión, perfil y búsqueda de usuarios")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private RegisteredUserRepository registeredUserRepository;

    @PostMapping("/register")
    @Operation(summary = "Registrar usuario", description = "Registra un nuevo usuario y devuelve tokens de autenticación")
    public ResponseEntity<?> register(@Valid @RequestBody RegisteredUserDTO request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario con email y contraseña. Puede requerir 2FA")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (com.desarrollo.raffy.exception.TwoFARequiredException e) {
            return ResponseEntity.status(401).body(java.util.Map.of("requires2FA", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refrescar token", description = "Genera un nuevo token de acceso a partir de un refresh token válido")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            AuthResponse response = authService.refreshToken(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Perfil actual", description = "Obtiene la información del usuario autenticado")
    public ResponseEntity<UserResponse> getCurrentUser() {
        try {
            UserResponse user = authService.getCurrentUser();
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión", description = "Invalida el refresh token y cierra la sesión del usuario")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            authService.logout(request.getRefreshToken());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/update-profile")
    @Operation(summary = "Actualizar perfil", description = "Actualiza los datos del perfil del usuario autenticado")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        try {
            UserResponse updatedUser = authService.updateProfile(request);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/change-password")
    @Operation(summary = "Cambiar contraseña", description = "Permite al usuario cambiar su contraseña de forma segura")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        try {
            authService.changePassword(request);
            return ResponseEntity.ok().body("Contraseña actualizada correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/users/{nickname}")
    @Operation(summary = "Usuario por nickname", description = "Obtiene el perfil público de un usuario por su nickname")
    public ResponseEntity<UserResponse> getUserByNickname(@PathVariable String nickname) {
        try {
            UserResponse user = authService.getUserByNickname(nickname);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/users/search")
    @Operation(summary = "Buscar usuarios", description = "Busca usuarios por nickname, nombre o email")
    public ResponseEntity<List<UserResponse>> searchUsers(@Parameter(description = "Texto de búsqueda") @RequestParam("query") String query) {
        List<UserResponse> users = authService.searchUsersByQuery(query);
        return ResponseEntity.ok(users);
    }

    /**
     * Sirve la imagen de avatar del usuario por nickname para usar en Open Graph/Twitter.
     * Devuelve 404 si no hay imagen.
     */
    @GetMapping(value = "/users/{nickname}/avatar", produces = MediaType.IMAGE_JPEG_VALUE)
    @Operation(summary = "Avatar de usuario", description = "Devuelve la imagen de avatar del usuario para uso en Open Graph/Twitter")
    public ResponseEntity<byte[]> getAvatarByNickname(@PathVariable String nickname) {
        RegisteredUser user = registeredUserRepository.findByNickname(nickname)
                .orElse(null);
        if (user == null || user.getImagen() == null || user.getImagen().length == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(user.getImagen());
    }

    /**
     * Sirve la imagen de portada del usuario por nickname para usar en Open Graph/Twitter.
     * Devuelve 404 si no hay imagen.
     */
    @GetMapping(value = "/users/{nickname}/cover", produces = MediaType.IMAGE_JPEG_VALUE)
    @Operation(summary = "Portada de usuario", description = "Devuelve la imagen de portada del usuario para uso en Open Graph/Twitter")
    public ResponseEntity<byte[]> getCoverByNickname(@PathVariable String nickname) {
        RegisteredUser user = registeredUserRepository.findByNickname(nickname)
                .orElse(null);
        if (user == null || user.getCoverImage() == null || user.getCoverImage().length == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(user.getCoverImage());
    }
}
