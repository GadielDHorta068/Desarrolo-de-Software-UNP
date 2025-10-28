package com.desarrollo.raffy.model;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Column;

@Entity
@Table(name = "payments",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"payment_id"})
    }
)

@Setter
@Getter
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true)
    private Long id;

    @Column(name = "payment_id", unique = true)
    private String paymentId;           // ID del pago en MercadoPago/Stripe
    
    @Column(name = "external_reference")
    private String externalReference;   // ID interno de la orden
    
    @Column(name = "user_id")
    private String userId;              // ID del comprador en tu sistema

    @Column(name = "event_id")
    private Long eventId;               // ID del evento en tu sistema

    @Column(name = "receiver_id")
    private Long receiverId;              // ID del ticket en tu sistema
    
    @Column(name = "amount")
    private Double amount;              // Monto total
    
    @Column(name = "currency")
    private String currency;            // Moneda (ej. ARS)
    
    @Column(name = "payment_method_id")
    private String paymentMethodId;     // Ej. visa, account_money
    
    @Column(name = "payment_type_id")
    private String paymentTypeId;       // Ej. credit_card, ticket
    
    @Column(name = "status")
    private String status;              // approved, pending, rejected
    
    @Column(name = "status_detail")
    private String statusDetail;        // Detalle del estado (ej. cc_rejected_insufficient_amount)
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;    // Fecha/hora de creación
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;    // Última actualización (opcional)
}