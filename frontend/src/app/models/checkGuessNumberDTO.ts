export interface CheckGuessNumberDTO {
    
    status: 'HIGHER' | 'LOWER' | 'WIN';
    message: string;
}