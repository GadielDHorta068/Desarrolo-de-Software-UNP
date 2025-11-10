package com.desarrollo.raffy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "review",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "event_id"})
    }
)

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor

public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @NotNull(message = "el evento debe estar definido")
    @ManyToOne
    @JoinColumn(name = "event_id", referencedColumnName = "id")
    private Events event;

    @NotNull(message = "El usuario debe estar definido")
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    private User user;
    
    @NotNull(message = "la puntualidad de delivery es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "delivery", nullable = false)
    private DeliveryStatus delivery;
    
    @NotNull(message = "la puntuacion es obligatoria")
    @Column(name = "score", nullable = false)
    @Min(0)
    @Max(5)
    private double score; 
    
    @Size(max = 350, message = "El comentario no puede superar los 350 caracteres")
    @Column(name = "comment", length = 350)
    private String comment;
}
