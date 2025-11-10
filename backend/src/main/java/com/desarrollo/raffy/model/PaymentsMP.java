package com.desarrollo.raffy.model;

import jakarta.persistence.Id;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

import com.desarrollo.raffy.model.PayerMP;

// modelo de pagos esperado por el api de MP
@Setter
@Getter
public class PaymentsMP {
    private String token;
    private String issuer_id;
    private String payment_method_id;
    private Double transaction_amount;
    private int installments;
    private PayerMP payer;

    public PayerMP getPayer() { return payer; }
    
}
