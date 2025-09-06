package com.desarrollo.raffy.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Clase para estructurar las respuestas de error de la API
 * Proporciona un formato consistente para todos los errores
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    /**
     * Timestamp del error
     */
    private LocalDateTime timestamp;
    
    /**
     * Código de estado HTTP
     */
    private int status;
    
    /**
     * Tipo de error
     */
    private String error;
    
    /**
     * Mensaje descriptivo del error
     */
    private String message;
    
    /**
     * Ruta donde ocurrió el error
     */
    private String path;
    
    /**
     * Errores de validación específicos (opcional)
     */
    private Map<String, String> validationErrors;
    
    /**
     * Detalles adicionales del error (opcional)
     */
    private Object details;
}

/*
 * @RestController
public class MiControlador {
    
    @GetMapping("/ejemplo")
    public ResponseEntity<?> ejemplo() {
        try {
            // Tu lógica aquí
            return ResponseEntity.ok("Éxito");
        } catch (Exception e) {
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .error("Error personalizado")
                    .message("Algo salió mal: " + e.getMessage())
                    .path("/ejemplo")
                    .build();
            
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }
}
 */