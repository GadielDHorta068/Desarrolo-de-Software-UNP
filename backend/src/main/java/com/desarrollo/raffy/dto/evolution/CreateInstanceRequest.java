package com.desarrollo.raffy.dto.evolution;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateInstanceRequest {
    @NotBlank
    private String instanceName;
    private Boolean qrcode = true;
    private String token;
    private String number;
    private String integration = "WHATSAPP-BAILEYS";
}
