import { Component, inject, signal } from '@angular/core';
import { RouterOutlet, RouterLink, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Auth } from './core/auth/auth';
import { NotificationService, AppNotification } from './core/services/notification.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, CommonModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  private auth = inject(Auth);
  private router = inject(Router);
  private notificationService = inject(NotificationService);
  
  isMenuOpen = false;
  isNotifOpen = false;
  unreadCount = 0;
  notifications: AppNotification[] = [];

  constructor() {
    // If user is already logged in on startup
    if (this.isAuthenticated()) {
      this.notificationService.initialize();
    }

    this.notificationService.unreadNotifications$.subscribe(notifs => {
      this.notifications = notifs;
      this.unreadCount = notifs.length;
    });
  }

  toggleMenu() {
    this.isMenuOpen = !this.isMenuOpen;
    if (this.isMenuOpen) {
      document.body.style.overflow = 'hidden'; // Prevents background scrolling
    } else {
      document.body.style.overflow = '';
    }
  }

  closeMenu() {
    this.isMenuOpen = false;
    document.body.style.overflow = '';
  }

  goHome() {
    this.closeMenu();
    if (this.isAuthenticated()) {
      this.router.navigate(['/dashboard']);
    } else {
      this.router.navigate(['/login']);
    }
  }

  isAuthenticated(): boolean {
    return this.auth.isLoggedIn();
  }

  isProvider(): boolean {
    const user = this.auth.getUser();
    return user && user.role === 'PROVIDER';
  }

  logout() {
    this.auth.logout();
    this.notificationService.disconnect();
    this.closeMenu();
    this.router.navigate(['/login']);
  }
  
  readNotification(id: string) {
    this.notificationService.markAsRead(id);
    // You could also navigate the user to the relevant page based on entityType here
  }
}
