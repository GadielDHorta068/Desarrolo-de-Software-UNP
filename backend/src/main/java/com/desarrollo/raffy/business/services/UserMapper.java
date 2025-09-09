package com.desarrollo.raffy.business.services;

import com.desarrollo.raffy.dto.RegisteredUserDTO;
import com.desarrollo.raffy.dto.UserDTO;
import com.desarrollo.raffy.model.RegisteredUser;
import com.desarrollo.raffy.model.User;

public class UserMapper {

    public static UserDTO toDTO(User aUser) {
        if (aUser instanceof RegisteredUser registeredUser) {
            RegisteredUserDTO result = new RegisteredUserDTO();
            result.setNickname(registeredUser.getNickname());
            result.setName(registeredUser.getName());
            result.setSurname(registeredUser.getSurname());
            result.setEmail(registeredUser.getEmail());
            result.setCellphone(registeredUser.getCellphone());
            result.setImagen(registeredUser.getImagen());

            return result;
        }
        else {
            UserDTO result = new UserDTO();
            result.setName(aUser.getName());
            result.setSurname(aUser.getSurname());
            result.setEmail(aUser.getEmail());
            result.setCellphone(aUser.getCellphone());           
            
            return result;
        }
    }

}
