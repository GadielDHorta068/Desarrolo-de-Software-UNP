package com.desarrollo.raffy.business.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.desarrollo.raffy.dto.AuthResponse;
import com.desarrollo.raffy.dto.ChangePasswordRequest;
import com.desarrollo.raffy.dto.LoginRequest;
import com.desarrollo.raffy.dto.RefreshTokenRequest;
import com.desarrollo.raffy.dto.RegisteredUserDTO;
import com.desarrollo.raffy.dto.UpdateProfileRequest;
import com.desarrollo.raffy.dto.UserResponse;
import com.desarrollo.raffy.exception.UserAlreadyExistsException;
import com.desarrollo.raffy.model.RefreshToken;
import com.desarrollo.raffy.model.RegisteredUser;
import com.desarrollo.raffy.business.repository.RegisteredUserRepository;
import com.desarrollo.raffy.util.ImageUtils;

@Service
@Transactional
public class AuthService {

    @Autowired
    private RegisteredUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private EmailService emailService;

    public AuthResponse register(RegisteredUserDTO request) {
        try {
            // Verificar si el email ya existe
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new UserAlreadyExistsException("El email ya está registrado");
            }

            // Verificar si el nickname ya existe
            if (userRepository.existsByNickname(request.getNickname())) {
                throw new UserAlreadyExistsException("El nickname ya está en uso");
            }

            // Crear nuevo usuario
            RegisteredUser user = new RegisteredUser(
                    request.getName(),
                    request.getSurname(),
                    request.getEmail(),
                    request.getCellphone(),
                    request.getNickname(),
                    passwordEncoder.encode(request.getPassword()));
            user.setImagen(ImageUtils.base64ToBytes(request.getImagen()));

            RegisteredUser savedUser = userRepository.save(user);
            
            // Enviar correo de bienvenida con plantilla profesional
            String userName = savedUser.getName() + " " + savedUser.getSurname();
            emailService.sendWelcomeEmailWithTemplate(savedUser.getEmail(), userName, null);

            // Generar tokens
            String accessToken = jwtService.generateToken(savedUser);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser);

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .expiresIn(jwtService.getJwtExpiration())
                    .user(mapToUserResponse(savedUser))
                    .build();
                    
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Manejar errores de integridad de base de datos como fallback
            if (e.getMessage().contains("email") || e.getMessage().contains("UKoshmjvr6wht0bg9oivn75aajr")) {
                throw new UserAlreadyExistsException("El email ya está registrado");
            } else if (e.getMessage().contains("nickname")) {
                throw new UserAlreadyExistsException("El nickname ya está en uso");
            } else {
                
                throw new RuntimeException("Error al registrar usuario: " + e.getMessage());
            }
        } catch (UserAlreadyExistsException e) {
            // Re-lanzar excepciones de usuario ya existente
            throw e;
        } catch (Exception e) {
            // Manejar cualquier otro error
            throw new RuntimeException("Error al registrar usuario: " + e.getMessage());
        }
    }

    public AuthResponse login(LoginRequest request) {
        // Autenticar usuario
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        RegisteredUser user = (RegisteredUser) authentication.getPrincipal();

        // Generar tokens
        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        UserResponse userResponse = mapToUserResponse(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .expiresIn(jwtService.getJwtExpiration())
                .user(userResponse)
                .build();
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        return refreshTokenService.findByToken(request.getRefreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String accessToken = jwtService.generateToken(user);
                    return AuthResponse.builder()
                            .accessToken(accessToken)
                            .refreshToken(request.getRefreshToken())
                            .expiresIn(jwtService.getJwtExpiration())
                            .user(mapToUserResponse(user))
                            .build();
                })
                .orElseThrow(() -> new RuntimeException("Refresh token no válido"));
    }

    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof RegisteredUser) {
            RegisteredUser user = (RegisteredUser) authentication.getPrincipal();
            return mapToUserResponse(user);
        }
        throw new RuntimeException("Usuario no autenticado");
    }

    public void logout(String refreshToken) {
        refreshTokenService.findByToken(refreshToken)
                .ifPresent(token -> refreshTokenService.deleteByUser(token.getUser()));
    }

    public UserResponse updateProfile(UpdateProfileRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        RegisteredUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verificar si el nuevo email ya existe (si es diferente al actual)
        if (!user.getEmail().equals(request.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        // Verificar si el nuevo nickname ya existe (si es diferente al actual)
        if (!user.getNickname().equals(request.getNickname()) &&
                userRepository.existsByNickname(request.getNickname())) {
            throw new RuntimeException("El nickname ya está en uso");
        }

        // Actualizar los campos
        user.setName(request.getName());
        user.setSurname(request.getSurname());
        user.setEmail(request.getEmail());
        user.setCellphone(request.getCellphone());
        user.setNickname(request.getNickname());
        user.setImagen(ImageUtils.base64ToBytes(request.getImagen()));
        // Imagen de portada opcional
        user.setCoverImage(ImageUtils.base64ToBytes(request.getCoverImage()));
        // Descripción opcional: si viene vacía o null, guardar como null
        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            user.setDescription(null);
        } else {
            user.setDescription(request.getDescription().trim());
        }

        // Redes sociales opcionales: guardar null si vienen vacías
        user.setTwitter(request.getTwitter() != null && !request.getTwitter().trim().isEmpty() ? request.getTwitter().trim() : null);
        user.setFacebook(request.getFacebook() != null && !request.getFacebook().trim().isEmpty() ? request.getFacebook().trim() : null);
        user.setInstagram(request.getInstagram() != null && !request.getInstagram().trim().isEmpty() ? request.getInstagram().trim() : null);
        user.setLinkedin(request.getLinkedin() != null && !request.getLinkedin().trim().isEmpty() ? request.getLinkedin().trim() : null);

        RegisteredUser updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    public void changePassword(ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        RegisteredUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verificar que la contraseña actual sea correcta
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("La contraseña actual es incorrecta");
        }

        // Hashear y guardar la nueva contraseña
        String hashedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(hashedNewPassword);

        userRepository.save(user);
    }

    public UserResponse getUserByNickname(String nickname) {
        RegisteredUser user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con nickname: " + nickname));
        return mapToUserResponse(user);
    }

    private UserResponse mapToUserResponse(RegisteredUser user) {
        String imagenBase64 = null;
        String coverBase64 = null;
        try {
            // Manejar el campo LOB de manera segura
            if (user.getImagen() != null) {
                imagenBase64 = ImageUtils.bytesToBase64(user.getImagen());
            }
            if (user.getCoverImage() != null) {
                coverBase64 = ImageUtils.bytesToBase64(user.getCoverImage());
            }
        } catch (Exception e) {
            // Log del error pero continuar sin la imagen
            System.err.println("Error al procesar imagen del usuario " + user.getId() + ": " + e.getMessage());
            imagenBase64 = null;
            coverBase64 = null;
        }

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .surname(user.getSurname())
                .email(user.getEmail())
                .cellphone(user.getCellphone())
                .nickname(user.getNickname())
                .userType(user.getUserType())
                .imagen(imagenBase64)
                .coverImage(coverBase64)
                .description(user.getDescription())
                .twitter(user.getTwitter())
                .facebook(user.getFacebook())
                .instagram(user.getInstagram())
                .linkedin(user.getLinkedin())
                .build();
    }
}