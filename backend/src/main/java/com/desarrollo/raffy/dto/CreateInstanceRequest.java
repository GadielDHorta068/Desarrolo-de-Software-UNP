package com.desarrollo.raffy.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateInstanceRequest {
    @NotBlank
    private String instanceName;

    private String token; // opcional, si se desea definir apikey propio

    private Boolean qrcode = Boolean.TRUE; // generar QR automáticamente

    private String number; // número del propietario (opcional)

    private String integration = "WHATSAPP-BAILEYS"; // por defecto
}