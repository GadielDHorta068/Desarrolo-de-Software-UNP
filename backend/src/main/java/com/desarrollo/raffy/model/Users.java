package com.desarrollo.raffy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Users")

@Getter
@Setter

public abstract class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_seq")
    @SequenceGenerator(name = "user_id_seq", sequenceName = "user_id", initialValue = 0, allocationSize = 1)
    private Long id;

    private String name;

    private String surname;

    @Column(unique = true)
    @NotBlank(message = "El campo email es obligatorio") // pasar a ingles jaja
    @Email(message = "El Email debe tener un formato valido") // pasar a ingles jaja x2
    private String email;

    @Column(unique = true)
    private String cellphone;
}
