package com.desarrollo.raffy.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "guess_attempt")
public class GuessAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "contest_id")
    private GuessingContest contest;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "guessed_number", nullable = false)
    private int guessedNumber;

    @NotNull(message = "La fecha y hora del intento no debe ser nula")
    @Column(name = "attempt_time", nullable = false)
    private LocalDateTime attemptTime;
}
