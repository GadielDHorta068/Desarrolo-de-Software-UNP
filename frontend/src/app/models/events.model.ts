export enum StatusEvent {
    ACTIVE = 'ACTIVE',
    OPEN = 'OPEN',
    CLOSED = 'CLOSED',
    FINISHED = 'FINISHED',
    BLOCKED = 'BLOCKED'
}

export enum EventTypes {
    GIVEAWAY = 'GIVEAWAY',
    CONTEST = 'CONTEST',
    TOURNAMENT = 'TOURNAMENT',
    RAFFLES = 'RAFFLES'
}

export enum Category {
    SOLIDARIO = 'SOLIDARIO',
    DEPORTE = 'DEPORTE'
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
    category: {
        id: number;
        name: string;
    };
    statusEvent: StatusEvent;
    eventType: EventTypes;
    winnersCount: number;
    categoryId: number,
    categoryName: Category,
    creator: Creator
}