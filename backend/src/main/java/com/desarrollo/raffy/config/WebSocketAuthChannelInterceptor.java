package com.desarrollo.raffy.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import com.desarrollo.raffy.business.services.JwtService;

/**
 * Interceptor para autenticar conexiones STOMP usando JWT.
 * Extrae el token del header "Authorization" y establece el Principal
 * para que convertAndSendToUser funcione correctamente.
 */
@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = resolveAuthorizationHeader(accessor);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwt = authHeader.substring(7).trim();
                try {
                    String userEmail = jwtService.extractUsername(jwt);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                    if (jwtService.isTokenValid(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        accessor.setUser(authentication);
                    }
                } catch (Exception ignored) {
                    // Si el token es inválido o falla la validación, no establecer usuario
                }
            }
        }
        return message;
    }

    private String resolveAuthorizationHeader(StompHeaderAccessor accessor) {
        List<String> auth = accessor.getNativeHeader("Authorization");
        if (auth != null && !auth.isEmpty()) {
            return auth.get(0);
        }
        // Algunos clientes envían el header en minúsculas
        List<String> authLower = accessor.getNativeHeader("authorization");
        if (authLower != null && !authLower.isEmpty()) {
            return authLower.get(0);
        }
        return null;
    }
}