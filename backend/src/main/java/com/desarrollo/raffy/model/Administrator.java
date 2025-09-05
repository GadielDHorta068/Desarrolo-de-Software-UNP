package com.desarrollo.raffy.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class Administrator extends Users{
    
    private String password;

    private String imagen;
}
