package com.desarrollo.raffy.model;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "guest_user")
@PrimaryKeyJoinColumn(name = "id")
@Setter
@Getter
@NoArgsConstructor
public class GuestUser extends User {

    public GuestUser(String name, String surname, String email, String cellphone) {
        super(name, surname, email, cellphone);
    }
}
