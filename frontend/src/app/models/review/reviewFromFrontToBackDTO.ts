import { DeliveryStatus } from "./DeliveryStatus";

export interface reviewFromFrontToBackDTO {
    email: string,
    delivery: DeliveryStatus,
    score: number,
    comment: string
}