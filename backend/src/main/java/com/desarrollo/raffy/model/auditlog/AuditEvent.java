package com.desarrollo.raffy.model.auditlog;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.desarrollo.raffy.model.EventTypes;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor @AllArgsConstructor
@Setter @Getter
@Table(name = "audit_event")
public class AuditEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventId;

    @Column(name = "event_title", nullable = false)
    private String title;

    @Column(name = "creator_event", nullable = false)
    private String creatorEvent; // Le pasamos el nickname

    @Column(name = "related_event", nullable = false)
    private Long relatedEventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventTypes type;

    @Column(name = "event_start_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Column(name = "event_end_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Column(name = "seed", nullable = true)
    private Long seed; // Semilla utilizada en la selección de ganadores

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AuditAction> actions = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AuditParticipant> participants = new ArrayList<>();
}
