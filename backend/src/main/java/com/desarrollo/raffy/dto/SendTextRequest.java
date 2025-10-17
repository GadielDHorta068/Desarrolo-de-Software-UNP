package com.desarrollo.raffy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendTextRequest {
    @NotBlank
    private String instance;

    @NotBlank
    private String number;

    @NotBlank
    private String text;

    private Integer delay; // opcional
}