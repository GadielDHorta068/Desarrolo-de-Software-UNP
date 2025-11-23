import { Region } from "./region";

export interface UserDTO {
    name: string;
    surname: string;
    email: string;
    region: Region;
    cellphone: string;
}