package com.desarrollo.raffy.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@Table(name = "audit_logs")
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Identificador único del registro
    
    @Column(name = "execute_date", nullable = false)
    private LocalDateTime executeDate; // Fecha y hora de la ejecución
    
    @Column(name = "seed", nullable = false)
    private Long seed; // Semilla utilizada en la selección de ganadores
    
    @Column(name = "event_id", nullable = false)
    private String eventTitle; // Título del evento
    
    @Column(name = "event_type", nullable = false)
    private String eventType; // Tipo de evento
    
    @Column(name = "event_start_date", nullable = false)
    private LocalDate eventStartDate; // Fecha de inicio del evento
    
    @Column(name = "event_end_date", nullable = false)
    private LocalDate eventEndDate; // Fecha de fin del evento

   
}
