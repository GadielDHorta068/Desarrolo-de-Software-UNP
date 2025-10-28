package com.desarrollo.raffy.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;

import com.desarrollo.raffy.model.IdentificationMP;

/* @Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "categories") */

@Setter
@Getter
public class PayerMP {
    private String email;
    private IdentificationMP identification;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public IdentificationMP getIdentification() { return identification; }
    public void setIdentification(IdentificationMP identification) { this.identification = identification; }
}