export interface Raffle {
    id: number;
    title: string;
    description: string;
    startDate: number[];
    endDate: number[];
    organizer: string; // cambiar
    statusEvent: string; // cambiar
    eventType: string; // cambiar
    category: string; // cambiar
    cantOfWinners: number;
    imageUrl?: string;
    totalTickets: number;
    ticketPrice: string;
}