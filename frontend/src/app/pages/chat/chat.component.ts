import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ChatService } from '../../services/chat.service';
import { AuthService } from '../../services/auth.service';
import { Message } from '../../models/message.model';
import { Observable, Subscription, switchMap, map, take } from 'rxjs';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
  <div class="max-w-4xl mx-auto h-[80vh] flex flex-col bg-white/80 backdrop-blur rounded-xl shadow border border-gray-200">
    <div class="px-4 py-3 border-b border-gray-200 flex items-center justify-between">
      <div>
        <h2 class="text-lg font-semibold">Chat privado</h2>
        <p class="text-sm text-gray-500">Conversación uno a uno</p>
      </div>
    </div>

    <div class="flex-1 overflow-y-auto p-4 space-y-3" id="messages">
      <ng-container *ngFor="let msg of messages | async; trackBy: trackByMessage">
        <div [ngClass]="msg.remitenteId === myUserId ? 'flex justify-end' : 'flex justify-start'">
          <div class="max-w-[70%] px-4 py-2 rounded-2xl" [ngClass]="msg.remitenteId === myUserId ? 'bg-blue-600 text-white rounded-br-sm' : 'bg-gray-100 text-gray-800 rounded-bl-sm'">
            <p class="whitespace-pre-line">{{ msg.contenido }}</p>
            <div class="text-xs opacity-75 mt-1">
              {{ msg.fechaEnvio ? (msg.fechaEnvio | date:'short') : '' }}
            </div>
          </div>
        </div>
      </ng-container>
    </div>

    <form (ngSubmit)="send()" class="border-t border-gray-200 p-3 flex items-center gap-3">
      <input [(ngModel)]="newMessage" name="message" type="text" placeholder="Escribe un mensaje..." class="flex-1 px-4 py-2 rounded-full border focus:outline-none focus:ring-2 focus:ring-blue-500" />
      <button type="submit" class="px-5 py-2 rounded-full bg-blue-600 text-white font-medium hover:bg-blue-700">Enviar</button>
    </form>
  </div>
  `
})
export class ChatComponent implements OnInit, OnDestroy {
  destinatarioId!: number;
  myUserId?: number;
  messages!: Observable<Message[]>;
  newMessage = '';
  private sub?: Subscription;

  constructor(private route: ActivatedRoute, private chat: ChatService, private authService: AuthService) {}

  ngOnInit(): void {
    this.messages = this.chat.messages$;
    // Ejecutar la carga inicial una sola vez, evitando re-suscripciones
    this.sub = this.route.paramMap.pipe(
      take(1),
      map(params => Number(params.get('userId')))
    ).pipe(
      switchMap(id => {
        this.destinatarioId = id;
        return this.authService.getCurrentUser().pipe(take(1), map(user => ({ id, user })));
      })
    ).subscribe(({ id, user }) => {
      this.myUserId = user.id;
      this.chat.clearMessages();
      // Cargar historial primero y luego conectar para evitar replays del backlog
      this.chat.loadHistory(id).pipe(take(1)).subscribe(history => {
        this.chat.appendMessages(history);
        this.chat.connect();
      });
    });
  }

  send(): void {
    const content = this.newMessage.trim();
    if (!content) return;
    // Incluir remitenteId para que el mensaje optimista se muestre del lado correcto
    this.chat.sendMessage({ contenido: content, destinatarioId: this.destinatarioId, remitenteId: this.myUserId });
    this.newMessage = '';
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
    // Evitar duplicación de suscripciones al salir
    this.chat.disconnect();
  }

  trackByMessage(index: number, msg: Message): number {
    return msg.id ?? index;
  }
}