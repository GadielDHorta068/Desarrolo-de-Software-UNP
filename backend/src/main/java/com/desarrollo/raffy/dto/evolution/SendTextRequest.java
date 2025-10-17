package com.desarrollo.raffy.dto.evolution;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendTextRequest {
    @NotBlank
    private String instance;

    @NotBlank
    private String number;

    @NotBlank
    private String text;

    private Integer delay; // optional message delay in ms
}