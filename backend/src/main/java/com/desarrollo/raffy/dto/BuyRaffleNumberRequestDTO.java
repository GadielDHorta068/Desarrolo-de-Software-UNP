package com.desarrollo.raffy.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;


@Getter @Setter
@NoArgsConstructor

public class BuyRaffleNumberRequestDTO {
    
    
    @JsonProperty("aGuestUser")
    @NotNull(message = "Debe indicar un usuario válido")
    private UserDTO aGuestUser;

    @JsonProperty("someNumbersToBuy")
    @NotEmpty(message = "Debe seleccionar al menos un número para comprar")
    private List<Integer> someNumbersToBuy;

}
