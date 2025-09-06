package com.desarrollo.raffy.model;

import jakarta.persistence.Entity;

@Entity
public abstract class Users {

    private Long id;

    private String name;

    private String surname;

    private String email;

    private String cellphone;
}
