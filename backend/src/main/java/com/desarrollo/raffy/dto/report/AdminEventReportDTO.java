package com.desarrollo.raffy.dto.report;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.desarrollo.raffy.model.StatusEvent;
import com.desarrollo.raffy.model.StatusReport;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class AdminEventReportDTO {
    
    private Long id;
    private Long eventId;
    private String eventTitle;
    private LocalDate eventDate;
    private StatusEvent statusEvent;
    private StatusReport statusReport;

    
    private int totalReports;
    private int score;

    private List<ReviewReportDTO> reports;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
