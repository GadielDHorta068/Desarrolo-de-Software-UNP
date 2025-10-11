package com.desarrollo.raffy.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter @Getter
@Table(name = "audit_logs")
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Identificador único del registro

    @Column(name = "creator_nickname", nullable = false)
    private String creatorNickname;
    
    @Column(name = "execute_date", nullable = false)
    private LocalDateTime executeDate; // Fecha y hora de la ejecución
    
    @Column(name = "seed", nullable = true)
    private Long seed; // Semilla utilizada en la selección de ganadores
  
    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "event_title", nullable = false)
    private String eventTitle; // Título del evento
    
    @Column(name = "event_type", nullable = false)
    private EventTypes eventType; // Tipo de evento
    
    @Column(name = "event_start_date", nullable = false)
    private LocalDate eventStartDate; // Fecha de inicio del evento
    
    @Column(name = "event_end_date", nullable = false)
    private LocalDate eventEndDate; // Fecha de fin del evento

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "audit_log_id")
    List<AuditParticipant> participants = new ArrayList<>(); // Lista de participantes en el evento
}
