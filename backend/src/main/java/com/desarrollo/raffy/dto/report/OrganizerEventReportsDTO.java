package com.desarrollo.raffy.dto.report;

import com.desarrollo.raffy.model.StatusEvent;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class OrganizerEventReportsDTO {
    private Long eventId;
    private  String eventTitle;
    private StatusEvent statusEvent;
}
