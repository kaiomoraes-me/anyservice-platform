import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface UserProfile {
  id: number;
  name: string;
  email: string;
  role: 'CLIENT' | 'PROVIDER' | 'ADMIN';
  createdAt: string;
  bio?: string;
  phone?: string;
  phoneVisible?: boolean;
  phoneVerified?: boolean;
  profilePictureUrl?: string;
  username?: string;
}

export interface UserUpdate {
  name?: string;
  bio?: string;
  phoneVisible?: boolean;
  username?: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {

  // Apontando direto pro proxy (igual o auth.ts faz)
  private apiUrl = '/api/users';

  constructor(private http: HttpClient) { }

  getProfile(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.apiUrl}/me`);
  }

  updateProfile(updateData: UserUpdate): Observable<UserProfile> {
    return this.http.put<UserProfile>(`${this.apiUrl}/me`, updateData);
  }

  uploadAvatar(file: File): Observable<{ profilePictureUrl: string }> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<{ profilePictureUrl: string }>(`${this.apiUrl}/me/avatar`, formData);
  }

  sendPhoneCode(data: { countryCode: string, phone: string }): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/me/phone/send-code`, data);
  }

  verifyPhoneCode(data: { code: string }): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/me/phone/verify`, data);
  }
}
