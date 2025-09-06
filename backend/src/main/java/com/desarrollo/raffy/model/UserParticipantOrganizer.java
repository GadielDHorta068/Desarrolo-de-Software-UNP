package com.desarrollo.raffy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Organizer")

@Getter
@Setter
@NoArgsConstructor

public class UserParticipantOrganizer extends Users {

    @Column(unique = true)
    private String nickname;

    @Column(nullable = false)
    private String password;

    private UserTypes userType;
    
    private String imagen;

}