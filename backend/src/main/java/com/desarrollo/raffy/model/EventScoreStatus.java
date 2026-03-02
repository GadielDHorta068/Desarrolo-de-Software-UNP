package com.desarrollo.raffy.model;

public enum EventScoreStatus {
    ACTIVE("Activo"),
    WARNING("Advertencia"),
    REVIEW("En revisión"),
    SUSPENDED("Suspendido");

    private final String displayName;

    EventScoreStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName(){
        return displayName;
    }
}
