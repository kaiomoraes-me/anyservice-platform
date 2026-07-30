import { Component, OnInit, inject, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { OrderService, ChatMessage } from '../../core/services/order.service';
import { Auth } from '../../core/auth/auth';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './chat.html',
  styleUrl: './chat.css'
})
export class ChatComponent implements OnInit, OnDestroy {
  private route = inject(ActivatedRoute);
  private orderService = inject(OrderService);
  private auth = inject(Auth);
  private cdr = inject(ChangeDetectorRef);

  orderId!: number;
  messages: ChatMessage[] = [];
  newMessage = '';
  myUserId = this.auth.getUser()?.id;
  
  private pollInterval: any;

  ngOnInit() {
    this.orderId = Number(this.route.snapshot.paramMap.get('orderId'));
    this.loadMessages();
    
    // Polling simples de 3 em 3 segundos para ir buscar novas mensagens (em produção seria WebSockets)
    this.pollInterval = setInterval(() => {
      this.loadMessages();
    }, 3000);
  }

  ngOnDestroy() {
    if (this.pollInterval) {
      clearInterval(this.pollInterval);
    }
  }

  loadMessages() {
    this.orderService.getMessages(this.orderId).subscribe({
      next: (data) => {
        this.messages = data;
        this.cdr.detectChanges(); // Força a atualização do ecrã
      },
      error: (err) => console.error('Erro ao carregar mensagens', err)
    });
  }

  sendMessage() {
    if (!this.newMessage.trim()) return;
    
    this.orderService.sendMessage(this.orderId, this.newMessage).subscribe({
      next: (msg) => {
        this.messages.push(msg);
        this.newMessage = '';
        this.cdr.detectChanges();
      }
    });
  }
}
