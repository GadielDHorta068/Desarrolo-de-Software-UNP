export interface Message {
  id?: number;
  contenido: string;
  remitenteId?: number;
  destinatarioId: number;
  fechaEnvio?: string; // ISO string desde backend
  leido?: boolean;
}