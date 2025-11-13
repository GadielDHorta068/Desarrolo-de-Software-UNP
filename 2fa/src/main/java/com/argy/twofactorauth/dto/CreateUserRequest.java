/**
 * Petición para crear usuario.
 */
package com.argy.twofactorauth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class CreateUserRequest {
    @NotBlank
    @Size(min = 3, max = 128)
    private String username;
}