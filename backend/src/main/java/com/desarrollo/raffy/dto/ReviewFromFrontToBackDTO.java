package com.desarrollo.raffy.dto;

import com.desarrollo.raffy.model.DeliveryStatus;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data

@Getter @Setter
@NoArgsConstructor

public class ReviewFromFrontToBackDTO {
    private String email;
    private int score;
    private DeliveryStatus delivery;
    private String comment;
}
