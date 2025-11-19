package com.desarrollo.raffy.dto;

import com.desarrollo.raffy.model.DeliveryStatus;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data

@Getter @Setter
@NoArgsConstructor

public class ReviewFromBackToFrontDTO {
    private Long eventId;
    private String eventTitle;
    private String name;
    private String surname;
    private String nickname;
    private Double score;
    private DeliveryStatus delivery;
    private String comment;
}
