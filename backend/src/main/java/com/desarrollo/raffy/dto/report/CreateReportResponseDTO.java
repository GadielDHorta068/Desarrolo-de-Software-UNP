package com.desarrollo.raffy.dto.report;

import java.time.LocalDateTime;

import com.desarrollo.raffy.model.StatusReport;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class CreateReportResponseDTO {
    private String eventTitle;
    private StatusReport statusReport;
    private LocalDateTime timestamp;
}
