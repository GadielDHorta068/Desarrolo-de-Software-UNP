package com.desarrollo.raffy.dto.report;

import java.time.LocalDateTime;

import com.desarrollo.raffy.model.StatusReport;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

 @Getter @Setter
 @AllArgsConstructor @NoArgsConstructor
public class AdminReportDTO {
    private Long reportId;
    private String mailUserReport;
    private String reason;
    private StatusReport status;
    private LocalDateTime createdAt;
}
