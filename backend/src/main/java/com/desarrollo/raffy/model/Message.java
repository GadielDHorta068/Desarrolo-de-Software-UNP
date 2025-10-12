package com.desarrollo.raffy.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "message")
@Getter
@Setter
@NoArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String contenido;

    @Column(nullable = false, name = "remitente_id")
    private Long remitenteId;

    @Column(nullable = false, name = "destinatario_id")
    private Long destinatarioId;

    @Column(nullable = false, name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    @Column(nullable = false)
    private Boolean leido = false;
}