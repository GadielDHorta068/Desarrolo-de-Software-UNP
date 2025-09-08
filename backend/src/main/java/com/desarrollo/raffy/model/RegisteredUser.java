package com.desarrollo.raffy.model;

import org.hibernate.engine.jdbc.env.spi.NameQualifierSupport;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "RegisteredUser")

@Getter
@Setter
@NoArgsConstructor

public class RegisteredUser extends User {

    @Column(unique = true)
    private String nickname;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private UserType userType;
    
    @Column(nullable = false)
    private String imagen;

    public RegisteredUser(String name, String surname, String email, String cellphone, String nickname, String password) {
        this.setName(name);
        this.setSurname(surname);
        this.setEmail(email);
        this.setCellphone(cellphone);
        this.setNickname(nickname);
        this.setPassword(password);
        this.setUserType(UserType.NORMAL);
    }

}