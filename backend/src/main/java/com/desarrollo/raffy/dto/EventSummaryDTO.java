package com.desarrollo.raffy.dto;

import java.time.LocalDate;

import com.desarrollo.raffy.model.EventTypes;
import com.desarrollo.raffy.model.StatusEvent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventSummaryDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private int winnersCount;
    private StatusEvent statusEvent;
    private EventTypes eventType;
    private Long categoryId;
    private String categoryName;
    private CreatorSummaryDTO creator;
    private String imageUrl;
}