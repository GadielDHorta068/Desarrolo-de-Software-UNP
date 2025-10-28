package com.desarrollo.raffy.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import com.desarrollo.raffy.model.PayerMP;
import com.desarrollo.raffy.model.IdentificationMP;

// vemos si lo sigo aca A.T
// @Data
@Setter
@Getter
public class CreatePaymentRequest {
    private String token;
    private String issuerId;
    private String paymentMethodId;
    private float transactionAmount;
    private int installments;
    private PayerMP payer;

    public PayerMP getPayer() { return payer; }
}