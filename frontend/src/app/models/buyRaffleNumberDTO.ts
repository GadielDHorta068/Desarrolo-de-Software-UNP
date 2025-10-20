import { UserDTO } from "./UserDTO";

export interface BuyRaffleNumberDTO {
    aGuestUser: UserDTO; 
    someNumbersToBuy: number[];
}