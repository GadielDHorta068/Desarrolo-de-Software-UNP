package com.desarrollo.raffy.model;

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

public class RegisteredUsers extends Users {

    @Column(unique = true)
    private String nickname;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private UserTypes userType;
    
    @Column(nullable = false)
    private String imagen;

}