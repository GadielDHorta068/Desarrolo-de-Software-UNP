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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "guess_progress")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class GuessProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "contest_id")
    private GuessingContest contest;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount = 0;

    @Column(name = "numbers_tried", nullable = false)
    private String numbersTried;

    @NotNull(message = "La fecha y hora del intento no debe ser nula")
    @Column(name = "attempt_time", nullable = false)
    private LocalDateTime attemptTime;

    @Column(name = "duration_seconds", nullable = false)
    private Long durationSeconds = 0L;

    @Column(name = "has_won", nullable = false)
    private boolean hasWon = false;

    @Column(name = "position", nullable = false)
    private short position = 0;
}
