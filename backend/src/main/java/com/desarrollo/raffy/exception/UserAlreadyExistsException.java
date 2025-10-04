package com.desarrollo.raffy.exception;

/**
 * Excepción personalizada para cuando un usuario ya existe.
 * Se lanza cuando se intenta registrar un usuario con email o nickname duplicado.
 */
public class UserAlreadyExistsException extends RuntimeException {
    
    public UserAlreadyExistsException(String message) {
        super(message);
    }
    
    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
