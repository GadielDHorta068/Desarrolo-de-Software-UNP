package com.desarrollo.raffy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GuestToRegisteredRequest {
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    private String email;
    
    @NotBlank(message = "El nickname es obligatorio")
    @Size(min = 3, max = 20, message = "El nickname debe tener entre 3 y 20 caracteres")
    private String nickname;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;
    
    private String imagen; // Base64 opcional
}
