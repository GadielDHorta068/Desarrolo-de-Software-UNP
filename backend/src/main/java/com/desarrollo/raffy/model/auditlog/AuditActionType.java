package com.desarrollo.raffy.model.auditlog;

public enum AuditActionType {
    EVENT_CREATED,
    EVENT_UPDATED,
    EVENT_EXECUTED, // cuando se ejecuta el random para seleccionar el ganador
    EVENT_CLOSED,
    EVENT_FINALIZED,

    USER_REGISTERED,
    USER_REGISTERED_FAILED,
    USER_UNREGISTERED,

    NUMBER_PURCHASED,
    NUMBER_PURCHASED_FAILED,
    NUMBER_PURCHASED_PENDING,

    ERROR_OCURRED,
    SYSTEM_EVENT
}
