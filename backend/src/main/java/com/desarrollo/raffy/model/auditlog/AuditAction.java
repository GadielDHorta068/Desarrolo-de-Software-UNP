package com.desarrollo.raffy.model.auditlog;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.desarrollo.raffy.util.JsonConverter;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class AuditAction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_identifier")
    private String actorIdentifier;
    
    @Column(name = "timestamp", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditActionType action;

    @Column(name = "details", nullable = true)
    private String details;

    @Column(name = "metadata", columnDefinition = "jsonb")
    @Convert(converter = JsonConverter.class)
    private Map<String, Object> metadata = new HashMap<>();

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private AuditEvent event;
}
