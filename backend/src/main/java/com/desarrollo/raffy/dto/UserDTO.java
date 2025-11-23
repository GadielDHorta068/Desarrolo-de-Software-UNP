package com.desarrollo.raffy.dto;

import com.desarrollo.raffy.model.Region;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor

public class UserDTO {

    private String name;
    private String surname;
    private String email;
    private Region region;
    private String cellphone;

}
