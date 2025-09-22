export enum StatusEvent {
    ACTIVE = 'active',
    OPEN = 'open',
    CLOSED = 'closed',
    FINISHED = 'finished',
    BLOCKED = 'blocked'
}

export enum EventTypes {
    GIVEAWAY = 'GIVEAWAY',
    CONTEST = 'CONTEST',
    TOURNAMENT = 'TOURNAMENT'
}

export enum Category {
    SOLIDARIO = 'Solidario',
    DEPORTE = 'Deportivo'
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