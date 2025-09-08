package com.desarrollo.raffy.model;

public enum UserType {
    NORMAL ("Normal"),
    ADMIN ("Administrador");

    private final String description;
    
    private UserType(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }

}
