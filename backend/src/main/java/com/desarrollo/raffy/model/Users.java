package com.desarrollo.raffy.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@NoArgsConstructor
@Getter
public class Users {

    private Long id;

    private String name;

    private String email;

    private String password;

    private String cellphone;

    private String imagen;

    private UserTypes userTypes;
}
