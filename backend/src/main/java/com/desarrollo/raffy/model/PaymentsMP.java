package com.desarrollo.raffy.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;

import com.desarrollo.raffy.model.PayerMP;

/* @Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "categories") */

@Setter
@Getter
public class PaymentsMP {
    
    private String token;
    private String issuerId;
    private String paymentMethodId;
    private float transactionAmount;
    private int installments;
    private PayerMP payer;

    public PayerMP getPayer() { return payer; }
    
}
