package com.desarrollo.raffy.dto.report;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class CreateReportDTO {
    
    @NotNull(message = "El Id del evento es requerido")
    private Long eventId;

    @NotBlank(message = "El email es requerido")
    @Email(message = "Email inválido")
    private String mailUserReport;

    @NotBlank(message = "El motivo es requerido")
    @Size(min = 10, max = 500, message = "El motivo debe tener entre 10 y 500 caracteres")
    private String reason;
}
