export interface GuessProgressDTO {
    attemptCount: number;
    numbersTried: string;
    hasWon: boolean;
    lastAttemptTime: Date;
    durationSeconds: number;
}