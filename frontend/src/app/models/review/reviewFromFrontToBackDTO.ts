import { AwardAlignment } from "./AwardAlignment";
import { CommunicationRating } from "./CommunicationRating";
import { DeliveryStatus } from "./DeliveryStatus";

export interface reviewFromFrontToBackDTO {
    email: string,
    delivery: DeliveryStatus | null,
    awardAlingment: AwardAlignment | null,
    communicationRating: CommunicationRating | null,
    score: number,
    comment: string
}