package com.desarrollo.raffy.dto;

import com.desarrollo.raffy.model.PaymentsMP;
import com.desarrollo.raffy.dto.DataPaymentMpDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PaymentMpDTO {
    private DataPaymentMpDTO paymentData;
    private PaymentsMP paymentMp;
}