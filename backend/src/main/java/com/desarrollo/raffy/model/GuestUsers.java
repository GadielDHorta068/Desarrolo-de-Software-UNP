package com.desarrollo.raffy.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "GuestUsers")

@Setter
@Getter
@NoArgsConstructor

public class GuestUsers extends Users {

}
