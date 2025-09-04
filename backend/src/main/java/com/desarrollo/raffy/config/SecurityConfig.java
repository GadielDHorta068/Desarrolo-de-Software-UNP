package com.desarrollo.raffy.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuración de seguridad para JWT y BCrypt
 * Esta clase carga las configuraciones desde el archivo .env
 */
@Configuration
public class SecurityConfig {

    @Value("${bcrypt.strength:12}")
    private int bcryptStrength;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private Long jwtRefreshExpiration;

    @Value("${jwt.issuer}")
    private String jwtIssuer;

    @Value("${jwt.audience}")
    private String jwtAudience;

    /**
     * Bean para el encoder de contraseñas usando BCrypt
     * La fuerza se configura desde la variable BCRYPT_STRENGTH en el .env
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(bcryptStrength);
    }

    // Getters para acceder a las configuraciones de JWT
    public String getJwtSecret() {
        return jwtSecret;
    }

    public Long getJwtExpiration() {
        return jwtExpiration;
    }

    public Long getJwtRefreshExpiration() {
        return jwtRefreshExpiration;
    }

    public String getJwtIssuer() {
        return jwtIssuer;
    }

    public String getJwtAudience() {
        return jwtAudience;
    }

    public int getBcryptStrength() {
        return bcryptStrength;
    }
}
