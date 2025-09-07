package com.desarrollo.raffy.dto;

import com.desarrollo.raffy.model.UserTypes;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisteredUserDTO {
    
    private Long id;
    
    @NotBlank(message = "Es requerido el nombre")
    private String name;
    
    @NotBlank(message = "Es requerido el apellido")
    private String surname;
    
    @NotBlank(message = "Es requerido el número de celular")
    private String cellphone;

    @NotBlank(message = "Es requerido el mail")
    @Email(message = "El correo electrónico debe ser válido")
    private String email;

    @NotBlank(message = "Es requerido el apodo")
    private String nickname;

    @NotBlank(message = "Es requerido la contraseña")
    private String password;

    @NotBlank(message = "Es requerido la imagen")
    private String imagen;

    private UserTypes userType;

}