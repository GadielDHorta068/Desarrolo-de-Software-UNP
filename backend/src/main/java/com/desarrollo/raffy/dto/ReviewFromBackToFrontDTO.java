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
    private String eventTitle;
    private String name;
    private String surname;
    private Double score;
    private DeliveryStatus delivery;
    private String comment;
}
