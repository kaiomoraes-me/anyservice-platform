import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { Client, Message } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Auth } from '../auth/auth';

export interface AppNotification {
  id: string;
  recipient: any;
  actor: any;
  entityType: string;
  entityId: number;
  actionType: string;
  readStatus: boolean;
  createdAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private http = inject(HttpClient);
  private auth = inject(Auth);
  
  private stompClient: Client | null = null;
  private unreadNotificationsSubject = new BehaviorSubject<AppNotification[]>([]);
  public unreadNotifications$ = this.unreadNotificationsSubject.asObservable();

  private get apiUrl() {
    return `/api/notifications`;
  }

  /** Inicializa o fetch HTTP (Fallback) e tenta ligar o STOMP WebSocket */
  initialize() {
    // 1. Fetch inicial (Source of Truth)
    this.fetchUnread();

    // 2. Ligar WebSocket (Real-time events)
    this.connectWebSocket();
  }

  private fetchUnread() {
    this.http.get<AppNotification[]>(`${this.apiUrl}/unread`).subscribe({
      next: (notifications) => {
        this.unreadNotificationsSubject.next(notifications);
      },
      error: (err) => console.error('Falha ao obter notificações (HTTP Fallback):', err)
    });
  }

  private connectWebSocket() {
    const token = this.auth.getToken();
    if (!token) return;

    this.stompClient = new Client({
      // Usa factory para instanciar o SockJS. Necessário para fallback HTTP se WS falhar
      webSocketFactory: () => new SockJS(`/ws-notifications`),
      connectHeaders: {
        Authorization: `Bearer ${token}`
      },
      debug: (msg) => console.log('[STOMP]:', msg),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.stompClient.onConnect = (frame) => {
      console.log('🔗 WebSocket Conectado');
      
      const user = this.auth.getUser();
      if (!user) return;
      
      this.stompClient?.subscribe(`/user/queue/notifications`, (message: Message) => {
        if (message.body) {
          const newNotif: AppNotification = JSON.parse(message.body);
          // Atualiza o estado RxJS instantaneamente (prepend)
          const current = this.unreadNotificationsSubject.getValue();
          // Idempotency check no front-end: ignora se já tivermos recebido esta exata notificação
          if (!current.some(n => n.id === newNotif.id)) {
            this.unreadNotificationsSubject.next([newNotif, ...current]);
          }
        }
      });
    };

    this.stompClient.onStompError = (frame) => {
      console.error('Broker reported error: ' + frame.headers['message']);
      console.error('Additional details: ' + frame.body);
    };

    this.stompClient.activate();
  }

  /**
   * Optimistic Update!
   * Tira instantaneamente a notificação do menu (RxJS). Faz a requisição PATCH em background.
   * Em caso de falha de rede pesada, a notificação voltará a aparecer silenciosamente (Rollback).
   */
  markAsRead(notificationId: string) {
    const currentState = this.unreadNotificationsSubject.getValue();
    
    // 1. Optimistic Update (UI reage instantaneamente)
    const optimisticState = currentState.filter(n => n.id !== notificationId);
    this.unreadNotificationsSubject.next(optimisticState);

    // 2. Chamada HTTP (Background)
    this.http.patch(`${this.apiUrl}/${notificationId}/read`, {}).subscribe({
      next: () => {
        // Sucesso silencioso
      },
      error: (err) => {
        console.error('Falha ao marcar como lida, revertendo estado (Rollback Optimistic Update)', err);
        // 3. Rollback
        this.unreadNotificationsSubject.next(currentState);
      }
    });
  }

  disconnect() {
    if (this.stompClient) {
      this.stompClient.deactivate();
      this.stompClient = null;
    }
  }
}
