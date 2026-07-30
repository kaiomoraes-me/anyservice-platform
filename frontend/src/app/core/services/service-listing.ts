import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ProviderInfo {
  id: number;
  name: string;
  profilePictureUrl?: string;
}

export interface ServiceListing {
  id: number;
  title: string;
  description: string;
  price: number;
  category: string;
  provider: ProviderInfo;
  createdAt: string;
}

export interface CreateServiceListingDto {
  title: string;
  description: string;
  price: number;
  category: string;
}

@Injectable({
  providedIn: 'root'
})
export class ServiceListingService {
  private apiUrl = '/api/services';

  constructor(private http: HttpClient) { }

  // 1. Pública: Obter todos os serviços (vitrine)
  getAllListings(category?: string): Observable<ServiceListing[]> {
    let url = this.apiUrl;
    if (category) {
      url += `?category=${encodeURIComponent(category)}`;
    }
    return this.http.get<ServiceListing[]>(url);
  }

  // 1.1 Pública: Obter um serviço específico por ID
  getListingById(id: number): Observable<ServiceListing> {
    return this.http.get<ServiceListing>(`${this.apiUrl}/${id}`);
  }

  // 1.2 Checkout (Pagamento)
  createCheckoutSession(serviceId: number): Observable<{ checkoutUrl: string }> {
    return this.http.post<{ checkoutUrl: string }>(`/api/payments/checkout/${serviceId}`, {});
  }

  // 2. Privada: Obter os serviços que eu criei
  getMyListings(): Observable<ServiceListing[]> {
    return this.http.get<ServiceListing[]>(`${this.apiUrl}/me`);
  }

  // 3. Privada: Criar novo serviço
  createListing(data: CreateServiceListingDto): Observable<ServiceListing> {
    return this.http.post<ServiceListing>(this.apiUrl, data);
  }

  // 4. Privada: Apagar serviço
  deleteListing(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }
}
