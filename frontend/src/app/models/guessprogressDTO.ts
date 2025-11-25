export interface GuessProgressDTO {
    attemptCount: number;
    numbersTried: string;
    hasWon: boolean;
    lastAttemptTime: Date;
    durationSeconds: number;
}

export interface DataPlayParticipantDTO {
    eventTitle: string;
    name: string,
    surname: string,
    email: string,
    cellphone: string;
    attemptCount: number;
    numbersTried: string;
    hasWon: boolean;
    lastAttemptTime: string;
    durationSeconds: number;
}