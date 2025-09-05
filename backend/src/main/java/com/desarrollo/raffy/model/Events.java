package com.desarrollo.raffy.model;

import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public abstract class Events {
    // atributos
    private Long id;
    
    private String title;
    
    private String description;
    
    private LocalDate startDate;
    
    private LocalDate endDate;

    private Categories categories;

    private StatusEvent statusEvent;

}
