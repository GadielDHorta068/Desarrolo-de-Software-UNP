import { AwardAlignment } from "./AwardAlignment";
import { CommunicationRating } from "./CommunicationRating";
import { DeliveryStatus } from "./DeliveryStatus";

export interface reviewFromBacktoFrontDTO {
    name: string,
    surname: string,
    nickname: string|null,
    eventTitle: string,
    eventId: string,
    delivery: DeliveryStatus,
    awardAlingment: AwardAlignment,
    communicationRating: CommunicationRating,
    score: number,
    comment: string
}