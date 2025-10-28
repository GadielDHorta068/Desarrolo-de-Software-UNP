package com.desarrollo.raffy.exception;

/**
 * Excepción personalizada para errores de validación en operaciones de pago.
 * Se lanza cuando los datos del pago no cumplen con las reglas de negocio.
 */
public class PaymentValidationException extends RuntimeException {
    
    public PaymentValidationException(String message) {
        super(message);
    }
    
    public PaymentValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}