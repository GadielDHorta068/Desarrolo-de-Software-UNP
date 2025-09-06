package com.desarrollo.raffy.model;

public enum UserTypes {
    NORMAL ("Normal"),
    ADMIN ("Administrador");

    private final String description;
    
    private UserTypes(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }

}
