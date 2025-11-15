import { DeliveryStatus } from "./DeliveryStatus";

export interface reviewFromBacktoFrontDTO {
    name: string,
    surname: string,
    eventTitle: string,
    eventId: string,
    delivery: DeliveryStatus,
    score: number,
    comment: string
}