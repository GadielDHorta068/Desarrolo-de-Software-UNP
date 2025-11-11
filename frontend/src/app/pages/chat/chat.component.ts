import { Component, OnDestroy, OnInit, ChangeDetectorRef, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ChatService } from '../../services/chat.service';
import { AuthService, UserResponse } from '../../services/auth.service';
import { Message } from '../../models/message.model';
import { Observable, Subscription, take } from 'rxjs';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
  <div class="max-w-4xl mx-auto h-[80vh] flex flex-col bg-white/80 dark:bg-gray-800/80 backdrop-blur rounded-xl shadow border border-gray-200 dark:border-gray-700">
    <div class="px-4 py-3 border-b border-gray-200 dark:border-gray-700 flex items-center justify-between">
      <div>
        <h2 class="text-lg font-semibold dark:text-gray-100">{{ peerUser ? (peerUser.name + ' ' + peerUser.surname) : 'Cargando usuario...' }}</h2>
        <p class="text-sm text-gray-500 dark:text-gray-400">{{ peerUser?.email || '' }}</p>
      </div>
    </div>

    <div class="flex-1 overflow-y-auto p-4 space-y-3" id="messages" #messagesContainer>
      <ng-container *ngFor="let msg of messages | async; trackBy: trackByMessage">
        <div [ngClass]="msg.remitenteId === myUserId ? 'flex justify-end' : 'flex justify-start'">
          <div class="max-w-[70%] px-4 py-2 rounded-2xl" [ngClass]="msg.remitenteId === myUserId ? 'bg-blue-600 text-white rounded-br-sm' : 'bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-100 rounded-bl-sm'">
            <p class="whitespace-pre-line">{{ msg.contenido }}</p>
            <div class="text-xs opacity-75 mt-1 text-gray-500 dark:text-gray-400">
              {{ msg.fechaEnvio ? (msg.fechaEnvio | date:'short') : '' }}
            </div>
          </div>
        </div>
      </ng-container>
    </div>

    <form (ngSubmit)="send()" class="border-t border-gray-200 dark:border-gray-700 p-3 flex items-center gap-3">
      <input [(ngModel)]="newMessage" name="message" type="text" placeholder="Escribe un mensaje..." class="flex-1 px-4 py-2 rounded-full border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 placeholder-gray-400 dark:placeholder-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500" />
      <button type="submit" class="px-5 py-2 rounded-full bg-blue-600 text-white font-medium hover:bg-blue-700">Enviar</button>
    </form>
  </div>
  `
})
export class ChatComponent implements OnInit, OnDestroy, AfterViewInit {
  destinatarioId!: number;
  myUserId?: number;
  messages!: Observable<Message[]>;
  newMessage = '';
  private sub?: Subscription;
  private messagesSub?: Subscription;
  peerUser?: UserResponse;
  @ViewChild('messagesContainer') private messagesEl?: ElementRef<HTMLDivElement>;

  constructor(private route: ActivatedRoute, private chat: ChatService, private authService: AuthService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.messages = this.chat.messages$;
    // Suscribirse a cambios de mensajes para autoscroll
    this.messagesSub = this.chat.messages$.subscribe(() => this.scheduleScroll());
    // Obtener el ID del destinatario de forma síncrona y arrancar flujo
    const id = Number(this.route.snapshot.paramMap.get('userId'));
    this.destinatarioId = id;
    // Establecer conversación activa en el servicio para filtrar mensajes
    this.chat.setActivePeer(this.destinatarioId);

    // Cargar usuario actual sin bloquear el resto
    this.sub = this.authService.getCurrentUser().pipe(take(1)).subscribe(user => {
      this.myUserId = user.id;
    });

    // Limpiar y precargar datos del destinatario e historial en paralelo
    this.chat.clearMessages();
    this.authService.getUserById(id).pipe(take(1)).subscribe(peer => { this.peerUser = peer; this.cdr.detectChanges(); });

    this.chat.loadHistory(id).pipe(take(1)).subscribe({
      next: history => { this.chat.appendMessages(history); this.cdr.detectChanges(); this.scheduleScroll(); },
      error: () => {},
      complete: () => {
        // Conectar WS después de intentar cargar historial, exitoso o no
        this.chat.connect();
        // Marcar como leído tras abrir (después de cargar y conectar)
        this.chat.markAsRead(this.destinatarioId).pipe(take(1)).subscribe({
          next: () => {},
          error: () => {}
        });
        this.scheduleScroll();
      }
    });
  }

  ngAfterViewInit(): void {
    // Asegurar autoscroll una vez que la vista esté lista
    this.scheduleScroll();
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
    this.messagesSub?.unsubscribe();
    // Evitar duplicación de suscripciones al salir
    this.chat.setActivePeer(null);
    this.chat.disconnect();
  }

  trackByMessage(index: number, msg: Message): number {
    return msg.id ?? index;
  }

  private scheduleScroll(): void {
    // Esperar al siguiente frame para que el DOM se haya pintado
    requestAnimationFrame(() => {
      this.scrollToBottom();
      // Fallback por si aún no se calculó el layout completo
      setTimeout(() => this.scrollToBottom(), 50);
    });
  }

  private scrollToBottom(): void {
    const el = this.messagesEl?.nativeElement;
    if (!el) return;
    el.scrollTop = el.scrollHeight;
  }
}