package com.desarrollo.raffy.exception;

/**
 * Excepción personalizada para cuando no se encuentra un pago.
 * Se lanza cuando se intenta acceder a un pago que no existe en la base de datos.
 */
public class PaymentNotFoundException extends RuntimeException {
    
    public PaymentNotFoundException(String message) {
        super(message);
    }
    
    public PaymentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public PaymentNotFoundException(Long paymentId) {
        super("Pago no encontrado con ID: " + paymentId);
    }
}