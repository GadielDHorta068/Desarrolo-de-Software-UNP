export interface Response {
    timestamp: number[];
    path: string|null;
    data: any;
    message: string;
    error: string|null;
    status: number
}

export interface DataStatusEvent {
    id: number;
    status: string;
}

export interface ResponseInscript {
    data: any;
    message: string;
    status: number;
}

