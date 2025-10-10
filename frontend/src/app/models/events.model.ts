export enum StatusEvent {
    ACTIVE = 'ACTIVE',
    OPEN = 'OPEN',
    CLOSED = 'CLOSED',
    FINISHED = 'FINISHED',
    BLOCKED = 'BLOCKED'
}

export enum EventTypes {
    GIVEAWAY = 'GIVEAWAYS',     // sorteo
    CONTEST = 'CONTEST',
    TOURNAMENT = 'TOURNAMENT',
    RAFFLES = 'RAFFLES'         // rifa
}

export enum Category {
    SOLIDARIO = 'SOLIDARIO',
    DEPORTE = 'DEPORTE'
}

export interface EventType {
    code: string;
    name: string;
}

export interface Events {
    id: number;
    title: string;
    description: string;
    startDate: string;
    endDate: string;
    category: {
        id: number;
        name: string;
    };
    statusEvent: StatusEvent;
    eventType: EventTypes;
    winnersCount: number;
}

export interface Creator {
    id: number,
    name: string,
    surname: string,
    nickname: string
}

// A.T: es lo que actualmente devuelve el servicio (modelo temporal?)
export interface EventsTemp {
    id: number;
    title: string;
    description: string;
    startDate: number[];
    endDate: number[];
    statusEvent: StatusEvent;
    eventType: EventTypes;
    winnersCount: number;
    categoryId: number,
    categoryName: Category,
    creator: Creator,
    imageUrl?: string; // Campo opcional para la imagen en base64
    isUserRegistered?: boolean; // Indica si el usuario actual está inscrito en el evento
    quantityOfNumbers: number;
    priceOfNumber: number;
}
// de momento no usamos extensiones sino un solo modelo de eventos con todos los atr (A.T)
export interface RaffleEvent extends EventsTemp {
    quantityOfNumbers: number;
    priceOfNumber: number;
}
export interface EventsCreate {
    title: string;
    description: string;
    endDate: number[];
    category: {
        id: number;
    };
    eventType: EventTypes;
    winnersCount: number;
    image: string;
    quantityOfNumbers: number;
    priceOfNumber: number;
}
// de momento no usamos extensiones sino un solo modelo de eventos con todos los atr (A.T)
export interface RaffleCreate extends EventsCreate {
    quantityOfNumbers: number;
    priceOfNumber: number;
}