import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ServiceOrder {
  id: number;
  serviceId: number;
  serviceTitle: string;
  clientName: string;
  providerName: string;
  totalPrice: number;
  providerEarnings: number;
  status: 'PENDING' | 'PAID' | 'COMPLETED' | 'CANCELLED';
  createdAt: string;
  myRole: 'CLIENT' | 'PROVIDER';
}

export interface ChatMessage {
  id: number;
  senderName: string;
  senderId: number;
  content: string;
  timestamp: string;
}

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private http = inject(HttpClient);

  // Pedidos
  getMyOrders(): Observable<ServiceOrder[]> {
    return this.http.get<ServiceOrder[]>('/api/orders/me');
  }

  // Chat
  getMessages(orderId: number): Observable<ChatMessage[]> {
    return this.http.get<ChatMessage[]>(`/api/chat/${orderId}`);
  }

  sendMessage(orderId: number, content: string): Observable<ChatMessage> {
    return this.http.post<ChatMessage>(`/api/chat/${orderId}`, { content });
  }
}
