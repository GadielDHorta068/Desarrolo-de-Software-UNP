package com.desarrollo.raffy.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.desarrollo.raffy.dto.WinnerDTO;

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
    
    private LocalDateTime executeDate; // Fecha y hora de la ejecución
    
    private Long seed; // Semilla utilizada en la selección de ganadores
    
    private String eventTitle; // Título del evento
    
    private String eventType; // Tipo de evento
    
    private LocalDate eventStartDate; // Fecha de inicio del evento
    
    private LocalDate eventEndDate; // Fecha de fin del evento
    
    private String categoryName; // Nombre de la categoría del evento
}
