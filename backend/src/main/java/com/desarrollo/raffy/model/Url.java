package com.desarrollo.raffy.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Entity
@Getter
@Setter
@Table(name = "urls")
public class Url {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "click_count")
    private Integer clickCount;

    @Column(name = "shortcode", unique = true)
    private String shortcode;

    @Column(name = "original_url")
    private String originalUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "is_single_use")
    private Boolean isSingleUse;

    @Column(name = "is_used")
    private Boolean isUsed;

    @Column(name = "event_id")
    private Long eventId;
}