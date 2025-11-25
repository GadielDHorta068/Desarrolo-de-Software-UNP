package com.desarrollo.raffy.model;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter; 
import lombok.AllArgsConstructor;

import com.desarrollo.raffy.util.OnCreate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Setter @Getter
@NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "event")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Events {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "El creador del evento no debe estar vacío")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // ver
    private RegisteredUser creator;

    @NotBlank(message = "El titulo debe estar completo")
    @Size(min = 3, max = 50, message = "El titulo debe tener entre 3 y 50 caracteres")
    @Column(name = "title")
    private String title;

    @NotBlank(message = "La descripción debe estar completa")
    @Size(min = 3, max = 200, message = "La descripción debe tener entre 3 y 200 caracteres")
    @Column(name = "description")
    private String description;

    @NotNull(message = "Se debe tener fecha de inicio")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull(message = "Se debe tener una fecha de finalización")
    @Future(groups = OnCreate.class, message = "La fecha de finalización debe ser futura")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @NotNull(message = "Debe tener categoría")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // ver
    private Categories category;

    @NotNull(message = "El estado del evento no debe estar vacío")
    @Enumerated(EnumType.STRING)
    @Column(name = "status_event", nullable = false)
    private StatusEvent statusEvent;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(
        name = "event_participants",
        joinColumns = @JoinColumn(name = "event_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    private List<User> participants;

    @NotNull(message = "Debe especificar el tipo de evento")
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventTypes eventType;

    @Column(name = "image")
    private byte[] imagen;

    @Transient
    @JsonProperty("image")
    private String imageBase64;
    /*
     * En esta funcion se valida que la fecha de inicio no sea posterior a la fecha fin
     * @throws IllegalArgumentException si la fecha de finalización es anterior a la fecha de inicio
     */
    @PrePersist
    @PreUpdate
    private void validateDates() {
        if (startDate != null && endDate != null && !endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("La fecha de finalización debe ser posterior a la fecha de inicio");
        }//atajar el pancho del error en el presenter y cear un GlobalExceptionHandler
    }

    //Cuantos ganadores va a tener el sorteo
    @Column(name = "winners_count", nullable = false)
    private int winnersCount;

    @Column(name = "is_private", nullable = false, columnDefinition = "boolean default false")
    @com.fasterxml.jackson.annotation.JsonProperty("isPrivate")
    private boolean isPrivate = false;

}
