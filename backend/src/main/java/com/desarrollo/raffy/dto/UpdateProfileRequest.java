package com.desarrollo.raffy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    
    @NotBlank(message = "El nombre es requerido")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String name;
    
    @NotBlank(message = "El apellido es requerido")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    private String surname;
    
    @NotBlank(message = "El email es requerido")
    @Email(message = "El email debe tener un formato válido")
    private String email;
    
    @Size(max = 15, message = "El número de teléfono no puede exceder 15 caracteres")
    private String cellphone;
    
    @NotBlank(message = "El nickname es requerido")
    @Size(min = 3, max = 30, message = "El nickname debe tener entre 3 y 30 caracteres")
    private String nickname;
    
    private String imagen; // Base64 encoded image

    private String coverImage; // Base64 encoded cover image

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String description; // Optional user description

    // Social media links (optional)
    @Size(max = 255, message = "El enlace de Twitter no puede exceder 255 caracteres")
    private String twitter;

    @Size(max = 255, message = "El enlace de Facebook no puede exceder 255 caracteres")
    private String facebook;

    @Size(max = 255, message = "El enlace de Instagram no puede exceder 255 caracteres")
    private String instagram;

    @Size(max = 255, message = "El enlace de LinkedIn no puede exceder 255 caracteres")
    private String linkedin;
}