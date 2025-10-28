import { EventTypes } from "./events.model";

export interface AuditEvent {
    
    id: number;
    
    relatedEventId: number;

    title: string;
    
    creatorEvent: string;

    type: EventTypes;

    startDate: Date;
    
    endDate: Date;
    
    seed: number;

    actions: AuditAction[];

    participants: AuditParticipant[];

}

export interface AuditParticipant{
    id: number;

    userPosition: number;
    
    userName: string;

    userLastName: string;

    userEmail: string;

    userPhone: string;
}

export interface AuditAction {
    id: Number;

    actorIdentifier: string;

    timestamp: Date;

    action: AuditActionType;

    details: string;
}

export enum AuditActionType {
    EVENT_CREATED = 'EVENT_CREATED',
    EVENT_UPDATED = 'EVENT_UPDATED',
    EVENT_EXECUTED = 'EVENT_EXECUTED',
    EVENT_CLOSED = 'EVENT_CLOSED',
    EVENT_FINALIZED = 'EVENT_FINALIZED',

    USER_REGISTERED = 'USER_REGISTERED',
    USER_REGISTERED_FAILED = 'USER_REGISTERED_FAILED',
    USER_UNREGISTERED = 'USER_UNREGISTERED',

    NUMBER_PURCHASED = 'NUMBER_PURCHASED',
    NUMBER_PURCHASED_FAILED = 'NUMBER_PURCHASED_FAILED',

    ERROR_OCURRED = 'ERROR_OCURRED',
    SYSTEM_EVENT = 'SYSTEM_EVENT'
}