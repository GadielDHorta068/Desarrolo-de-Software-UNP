/**
 * Controlador REST para gestión de usuarios y administración del sistema.
 * 
 * Este controlador proporciona endpoints para:
 * - Consultar el estado de autenticación de dos factores (2FA) de usuarios
 * - Crear nuevos usuarios en el sistema
 * - Obtener información detallada de usuarios
 * - Eliminar usuarios del sistema
 * - Consultar estadísticas del sistema
 * - Listar usuarios con información de 2FA
 * 
 * Todos los endpoints requieren autenticación apropiada y están protegidos
 * por el sistema de seguridad de Spring Security.
 * 
 * @author Sistema de Autenticación 2FA
 * @version 1.0
 */
package com.argy.twofactorauth.controller;

import com.argy.twofactorauth.dto.CreateUserRequest;
import com.argy.twofactorauth.dto.UserResponse;
import com.argy.twofactorauth.dto.TwoFactorStatusResponse;
import com.argy.twofactorauth.dto.SystemStatsResponse;
import com.argy.twofactorauth.entity.User;
import com.argy.twofactorauth.repository.UserRepository;
import com.argy.twofactorauth.entity.RecoveryCode;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UserManagementController {

    private final UserRepository userRepository;

    public UserManagementController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Obtiene el estado de autenticación de dos factores (2FA) para un usuario específico.
     * 
     * Este endpoint permite consultar si un usuario tiene habilitado 2FA y cuántos
     * códigos de recuperación disponibles tiene restantes.
     * 
     * @param username Nombre de usuario a consultar
     * @return ResponseEntity con TwoFactorStatusResponse que incluye:
     *         - username: Nombre del usuario
     *         - twoFactorEnabled: Estado de 2FA (true/false)
     *         - createdAt: Fecha de creación del usuario
     * @example GET /api/users/juan_perez/status
     * @response 200 { "username": "juan_perez", "twoFactorEnabled": true, "createdAt": "2024-01-01T00:00:00" }
     * @response 404 Usuario no encontrado
     */
    @GetMapping("/users/{username}/status")
    public ResponseEntity<TwoFactorStatusResponse> getTwoFactorStatus(@PathVariable String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        boolean twoFactorEnabled = user.isEnabled();
        int remainingCodes = 0;
        if (user.getRecoveryCodes() != null) {
            remainingCodes = (int) user.getRecoveryCodes().stream()
                .filter(rc -> !rc.isUsed())
                .count();
        }
        
        TwoFactorStatusResponse response = new TwoFactorStatusResponse(
            username, 
            twoFactorEnabled, 
            remainingCodes
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene información completa de un usuario específico.
     * 
     * Este endpoint permite consultar todos los datos de un usuario incluyendo
     * su estado de 2FA, fechas de creación y actualización.
     * 
     * @param username Nombre de usuario a consultar
     * @return ResponseEntity con UserResponse que incluye:
     *         - id: ID único del usuario
     *         - username: Nombre del usuario
     *         - twoFactorEnabled: Estado de 2FA (true/false)
     *         - createdAt: Fecha de creación
     *         - updatedAt: Fecha de última actualización
     * @example GET /api/users/juan_perez
     * @response 200 { "id": 1, "username": "juan_perez", "twoFactorEnabled": true, "createdAt": "2024-01-01T00:00:00", "updatedAt": "2024-01-02T00:00:00" }
     * @response 404 Usuario no encontrado
     */
    @GetMapping("/users/{username}")
    public ResponseEntity<UserResponse> getUser(@PathVariable String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        UserResponse response = new UserResponse(
            user.getId(),
            user.getUsername(),
            user.isEnabled(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Crea un nuevo usuario en el sistema.
     * 
     * Este endpoint permite crear un nuevo usuario con los datos proporcionados.
     * El usuario se crea sin 2FA habilitado por defecto.
     * 
     * @param request Datos del nuevo usuario (CreateUserRequest)
     * @return ResponseEntity con UserResponse del usuario creado
     * @requestbody { "username": "nuevo_usuario", "email": "usuario@ejemplo.com", "password": "SecurePass123!" }
     * @response 201 { "id": 1, "username": "nuevo_usuario", "twoFactorEnabled": false, "createdAt": "2024-01-01T00:00:00", "updatedAt": "2024-01-01T00:00:00" }
     * @response 409 Conflicto - Usuario ya existe
     */
    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        // Verificar si el usuario ya existe
        if (userRepository.findByUsername(request.getUsername()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        User savedUser = userRepository.save(user);
        
        UserResponse response = new UserResponse(
            savedUser.getId(),
            savedUser.getUsername(),
            false,
            savedUser.getCreatedAt(),
            savedUser.getUpdatedAt()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Elimina un usuario y todos sus datos asociados del sistema.
     * 
     * Este endpoint permite eliminar completamente un usuario incluyendo:
     * - Datos del usuario
     - Secretos TOTP
     * - Códigos de recuperación
     * - Cualquier dato relacionado
     * 
     * @param username Nombre de usuario a eliminar
     * @return ResponseEntity sin contenido (204 No Content)
     * @example DELETE /api/users/juan_perez
     * @response 204 No Content - Usuario eliminado exitosamente
     * @response 404 Usuario no encontrado
     */
    @DeleteMapping("/users/{username}")
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        userRepository.delete(user);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene estadísticas generales del sistema de autenticación.
     * 
     * Este endpoint proporciona métricas importantes sobre el uso de 2FA:
     * - Total de usuarios en el sistema
     * - Usuarios con 2FA habilitado
     * - Porcentaje de adopción de 2FA
     * 
     * Útil para monitoreo y análisis de seguridad.
     * 
     * @return ResponseEntity con SystemStatsResponse que incluye:
     *         - totalUsers: Número total de usuarios
     *         - usersWith2FA: Número de usuarios con 2FA habilitado
     *         - twoFactorEnabledPercentage: Porcentaje de usuarios con 2FA
     * @example GET /api/admin/stats
     * @response 200 { "totalUsers": 150, "usersWith2FA": 75, "twoFactorEnabledPercentage": 50.0 }
     */
    @GetMapping("/admin/stats")
    public ResponseEntity<SystemStatsResponse> getSystemStats() {
        List<User> allUsers = userRepository.findAll();
        long totalUsers = allUsers.size();
        long usersWith2FA = allUsers.stream()
            .filter(u -> u.getSecret() != null)
            .count();
        
        long totalRecoveryCodes = allUsers.stream()
            .mapToLong(u -> u.getRecoveryCodes() != null ? u.getRecoveryCodes().size() : 0)
            .sum();
            
        long usedRecoveryCodes = allUsers.stream()
            .flatMap(u -> u.getRecoveryCodes() != null ? u.getRecoveryCodes().stream() : List.<RecoveryCode>of().stream())
            .filter(RecoveryCode::isUsed)
            .count();
        
        double adoptionRate = totalUsers > 0 ? (double) usersWith2FA / totalUsers * 100 : 0;
        
        SystemStatsResponse response = new SystemStatsResponse(
            totalUsers,
            usersWith2FA,
            totalRecoveryCodes,
            usedRecoveryCodes,
            adoptionRate
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Lista todos los usuarios con 2FA habilitado.
     */
    @GetMapping("/admin/users")
    public ResponseEntity<List<UserResponse>> getUsersWith2FA() {
        List<User> usersWith2FA = userRepository.findAll().stream()
            .filter(u -> u.getSecret() != null)
            .collect(Collectors.toList());
        
        List<UserResponse> response = usersWith2FA.stream()
            .map(user -> new UserResponse(
                user.getId(),
                user.getUsername(),
                true,
                user.getCreatedAt(),
                user.getUpdatedAt()
            ))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
