import { GuessProgressDTO } from "./guessprogressDTO";
import { UserDTO } from "./UserDTO";

export interface ParticipantRequestDTO {
    user: UserDTO;
    guessProgress: GuessProgressDTO;
}