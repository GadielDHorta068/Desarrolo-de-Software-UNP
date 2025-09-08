package com.desarrollo.raffy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String surname;

    @Column(unique = true)
    @NotBlank(message = "El campo email es obligatorio")
    @Email(message = "El Email debe tener un formato valido")
    private String email;

    private String cellphone;
    
    public User() {
    }
    
    public User(String name, String surname, String email, String cellphone) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.cellphone = cellphone;
    }
}
