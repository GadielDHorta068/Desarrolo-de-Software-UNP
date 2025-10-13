import { EventTypes } from "./events.model";

export interface AuditLog {
    id: number;

    creatorNickname: string;

    executeDate: Date;

    seed: number;

    eventId: number;

    eventTitle: string;

    eventType: EventTypes;

    eventStartDate: Date;

    eventEndDate: Date;

    participants: AuditParticipant[];

}

export interface AuditParticipant{
    id: number;

    userName: string;

    userLastName: string;

    userEmail: string;

    userPhone: string;

    userPosition: number;
}