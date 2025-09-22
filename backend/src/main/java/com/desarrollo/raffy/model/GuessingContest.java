package com.desarrollo.raffy.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;


@Getter @Setter
@Entity
@Table(name = "guessing_contest")
public class GuessingContest extends Events {

   @Column(name = "min_value", nullable = false)
    private int minValue;

    @Column(name = "max_value", nullable = false)
    private int maxValue;

    @Column(name = "target_number", nullable = false)
    private int targetNumber;

    @Column(name = "max_attempts", nullable = false)
    private int maxAttempts;

    @OneToMany(mappedBy = "contest", cascade = CascadeType.ALL)
    private List<GuessAttempt> attempts;
}