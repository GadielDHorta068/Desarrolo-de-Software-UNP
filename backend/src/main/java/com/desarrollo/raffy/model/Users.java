package com.desarrollo.raffy.model;

import jakarta.persistence.Entity;

@Entity
public class Users {

    private Long id;

    private String name;

    private String email;

    private String cellphone;
}
