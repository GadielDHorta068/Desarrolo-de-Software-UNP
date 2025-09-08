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
import com.desarrollo.raffy.dto.LoginRequest;
import com.desarrollo.raffy.dto.RefreshTokenRequest;
import com.desarrollo.raffy.dto.RegisteredUserDTO;
import com.desarrollo.raffy.dto.UserResponse;
import com.desarrollo.raffy.model.RefreshToken;
import com.desarrollo.raffy.model.RegisteredUser;
import com.desarrollo.raffy.business.repository.RegisteredUserRepository;

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

    public AuthResponse register(RegisteredUserDTO request) {
        // Verificar si el email ya existe
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        // Verificar si el nickname ya existe
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new RuntimeException("El nickname ya está en uso");
        }

        // Crear nuevo usuario
        RegisteredUser user = new RegisteredUser(
                request.getName(),
                request.getSurname(),
                request.getEmail(),
                request.getCellphone(),
                request.getNickname(),
                passwordEncoder.encode(request.getPassword())
        );
        user.setImagen(request.getImagen());

        RegisteredUser savedUser = userRepository.save(user);

        // Generar tokens
        String accessToken = jwtService.generateToken(savedUser);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .expiresIn(jwtService.getJwtExpiration())
                .user(mapToUserResponse(savedUser))
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        // Autenticar usuario
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        RegisteredUser user = (RegisteredUser) authentication.getPrincipal();

        // Generar tokens
        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .expiresIn(jwtService.getJwtExpiration())
                .user(mapToUserResponse(user))
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

    private UserResponse mapToUserResponse(RegisteredUser user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .surname(user.getSurname())
                .email(user.getEmail())
                .cellphone(user.getCellphone())
                .nickname(user.getNickname())
                .userType(user.getUserType())
                .imagen(user.getImagen())
                .build();
    }
}