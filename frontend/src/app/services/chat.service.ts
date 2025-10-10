import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { BehaviorSubject, Observable } from 'rxjs';
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
    return this.http.get<Message[]>(`${environment.apiUrl}/api/chat/history/${destinatarioId}`);
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
    this.appendMessage({ ...message });
  }

  clearMessages(): void {
    this.messagesSubject.next([]);
    this.messageIds.clear();
  }

  private appendMessage(message: Message): void {
    if (message.id != null) {
      if (this.messageIds.has(message.id)) return;
      this.messageIds.add(message.id);
    }
    const current = this.messagesSubject.value;
    this.messagesSubject.next([...current, message]);
  }

  // Carga el historial en un solo lote para evitar múltiples re-renderizados
  appendMessages(messages: Message[]): void {
    if (!messages || messages.length === 0) return;
    const current = this.messagesSubject.value;
    const filtered = messages.filter(m => {
      if (m.id == null) return true;
      if (this.messageIds.has(m.id)) return false;
      this.messageIds.add(m.id);
      return true;
    });
    if (filtered.length === 0) return;
    this.messagesSubject.next([...current, ...filtered]);
  }
}