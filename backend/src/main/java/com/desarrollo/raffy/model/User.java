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

    @Column(nullable = false)
    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @Column(nullable = false)
    @NotBlank(message = "El apellido es obligatorio")
    private String surname;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "El campo email es obligatorio")
    @Email(message = "El Email debe tener un formato valido")
    private String email;

    @Column(nullable = true)
    private String cellphone;
    
    public User() {
    }
    
    public User(String name, String surname, String email, String cellphone) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.cellphone = cellphone;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((email == null) ? 0 : email.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        User other = (User) obj;
        if (email == null) {
            if (other.email != null)
                return false;
        } else if (!email.equals(other.email))
            return false;
        return true;
    }
}
