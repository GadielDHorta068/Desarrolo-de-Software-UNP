import { GuestUser } from "../pages/questionary/guestUser";

export interface BuyRaffleNumberDTO {
    aGuestUser: GuestUser;
    someNumbersToBuy: number[];
}