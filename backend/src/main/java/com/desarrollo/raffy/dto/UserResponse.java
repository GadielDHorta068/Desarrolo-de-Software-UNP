package com.desarrollo.raffy.dto;

import com.desarrollo.raffy.model.UserType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    
    private Long id;
    private String name;
    private String surname;
    private String email;
    private String cellphone;
    private String nickname;
    private UserType userType;
    private String imagen; // Base64 encoded image
    private String description; // Optional user description
    private String twitter;
    private String facebook;
    private String instagram;
    private String linkedin;
}