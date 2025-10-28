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

/* @Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "categories") */

@Setter
@Getter
public class IdentificationMP {
    private String type;
    private String number;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }
}