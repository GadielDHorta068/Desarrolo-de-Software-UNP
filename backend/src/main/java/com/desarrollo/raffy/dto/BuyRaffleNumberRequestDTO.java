package com.desarrollo.raffy.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import com.desarrollo.raffy.model.GuestUser;

@Getter @Setter

public class BuyRaffleNumberRequestDTO {
    
    @Valid
    @NotNull(message = "Debe indicar un usuario válido")
    private GuestUser aGuestUser;

    @NotEmpty(message = "Debe seleccionar al menos un número para comprar")
    private List<Integer> someNumbersToBuy;

}
