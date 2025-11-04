export interface WinnerDTO {
  participantId: number;
  name: string;
  surname: string;
  position: number;
  email: string;
  phone: string;
  eventId: number;
  eventTitle: string;
  raffleNumber?: number; // Número de la rifa (solo para eventos tipo raffle)
}
