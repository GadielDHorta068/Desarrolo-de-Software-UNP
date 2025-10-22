export interface Message {
  id?: number;
  contenido: string;
  remitenteId?: number;
  destinatarioId: number;
  fechaEnvio?: string; // ISO string desde backend
  leido?: boolean;
}

// Modelo de resumen de chats no leídos en el header
export interface UnreadChatSummary {
  peerId: number;
  peerDisplayName: string;
  unreadCount: number;
  lastMessageTimestamp: string; // ISO string
}