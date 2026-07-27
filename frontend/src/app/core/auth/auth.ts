import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs';

@Service()
export class Auth {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/auth';

  login(credentials: any) {
    return this.http.post<any>(`${this.apiUrl}/authenticate`, credentials).pipe(
      tap(response => {
        if (response.token) {
          localStorage.setItem('token', response.token);
        }
      })
    );
  }

  register(userData: any) {
    return this.http.post<any>(`${this.apiUrl}/register`, userData);
  }

  verifyAccount(data: { email: string, code: string }) {
    return this.http.post<any>(`${this.apiUrl}/verify`, data);
  }

  forgotPassword(email: string) {
    return this.http.post<any>(`${this.apiUrl}/forgot-password`, { email });
  }

  resetPassword(data: any) {
    return this.http.post<any>(`${this.apiUrl}/reset-password`, data);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  logout() {
    localStorage.removeItem('token');
  }
}
