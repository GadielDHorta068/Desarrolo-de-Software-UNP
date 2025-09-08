package com.desarrollo.raffy.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "GuestUser")

@Setter
@Getter
@NoArgsConstructor

public class GuestUser extends User {

}
