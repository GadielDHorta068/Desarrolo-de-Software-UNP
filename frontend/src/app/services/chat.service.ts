import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { BehaviorSubject, Observable, map } from 'rxjs';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Message } from '../models/message.model';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class ChatService {
  private wsUrl = `${environment.apiUrl.replace('/api', '')}/ws`;
  private client: Client | null = null;
  private privateSub: StompSubscription | null = null;

  private messagesSubject = new BehaviorSubject<Message[]>([]);
  public messages$ = this.messagesSubject.asObservable();
  private messageIds = new Set<number>();

  constructor(private http: HttpClient, private authService: AuthService) {}

  // Convierte formatos de fecha (ISO, epoch, arreglo [y,m,d,h,mm,ss]) a ISO string
  private toIsoString(dateLike: any): string | undefined {
    if (!dateLike) return undefined;
    try {
      if (Array.isArray(dateLike)) {
        const [y, m, d, hh = 0, mm = 0, ss = 0] = dateLike as number[];
        return new Date(y, (m ?? 1) - 1, d, hh, mm, ss).toISOString();
      }
      if (typeof dateLike === 'string') {
        return new Date(dateLike).toISOString();
      }
      if (typeof dateLike === 'number') {
        return new Date(dateLike).toISOString();
      }
      return new Date(dateLike).toISOString();
    } catch {
      return undefined;
    }
  }

  connect(): void {
    if (this.client && this.client.connected) {
      return;
    }

    const token = this.authService.getToken();
    this.client = new Client({
      webSocketFactory: () => new SockJS(this.wsUrl),
      connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
      reconnectDelay: 5000,
      debug: (msg: any) => { /* opcional: console.log(msg) */ },
    });

    this.client.onConnect = () => {
      // Suscribir a cola privada
      this.privateSub = this.client!.subscribe('/user/queue/private-messages', (frame: IMessage) => {
        try {
          const message: Message = JSON.parse(frame.body);
          this.appendMessage(message);
        } catch {}
      });
    };

    this.client.activate();
  }

  disconnect(): void {
    if (this.privateSub) {
      this.privateSub.unsubscribe();
      this.privateSub = null;
    }
    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }
  }

  loadHistory(destinatarioId: number): Observable<Message[]> {
    // Endpoint REST está bajo /api/chat en el backend
    return this.http.get<Message[]>(`${environment.apiUrl}/api/chat/history/${destinatarioId}`).pipe(
      // Normalizar a ISO string para que el date pipe funcione y respete el tipo
      map(list => list.map(m => ({
        ...m,
        fechaEnvio: this.toIsoString((m as any).fechaEnvio),
      })))
    );
  }

  sendMessage(message: Message): void {
    if (!this.client || !this.client.connected) {
      this.connect();
    }
    this.client!.publish({
      destination: '/app/chat.sendMessage',
      body: JSON.stringify(message),
    });
    // Optimista: agregar el mensaje localmente (remitente actual)
    this.appendMessage({
      ...message,
      // Si no viene fecha, usar ahora como ISO para que el pipe de fecha funcione
      fechaEnvio: message.fechaEnvio ? this.toIsoString((message as any).fechaEnvio) : new Date().toISOString(),
    });
  }

  clearMessages(): void {
    this.messagesSubject.next([]);
    this.messageIds.clear();
  }

  private appendMessage(message: Message): void {
    const normalized: Message = {
      ...message,
      fechaEnvio: this.toIsoString((message as any).fechaEnvio) ?? message.fechaEnvio,
    };
    if (normalized.id != null) {
      if (this.messageIds.has(normalized.id)) return;
      this.messageIds.add(normalized.id);
    }
    const current = this.messagesSubject.value;
    this.messagesSubject.next([...current, normalized]);
  }

  // Carga el historial en un solo lote para evitar múltiples re-renderizados
  appendMessages(messages: Message[]): void {
    if (!messages || messages.length === 0) return;
    const current = this.messagesSubject.value;
    const filtered = messages
      .map(m => ({
        ...m,
        fechaEnvio: this.toIsoString((m as any).fechaEnvio) ?? m.fechaEnvio,
      }))
      .filter(m => {
        if (m.id == null) return true;
        if (this.messageIds.has(m.id)) return false;
        this.messageIds.add(m.id);
        return true;
      });
    if (filtered.length === 0) return;
    this.messagesSubject.next([...current, ...filtered]);
  }
}