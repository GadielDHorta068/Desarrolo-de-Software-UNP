package com.desarrollo.raffy.model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Entity
public class Organizer extends Users{

    private String password;

    private String imagen;

}