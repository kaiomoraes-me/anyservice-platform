import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ServiceListingService, ServiceListing } from '../../core/services/service-listing';

@Component({
  selector: 'app-service-details',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './service-details.html',
  styleUrl: './service-details.css'
})
export class ServiceDetailsComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private serviceListing = inject(ServiceListingService);
  private cdr = inject(ChangeDetectorRef);

  service: ServiceListing | null = null;
  isLoading = true;
  errorMessage = '';

  ngOnInit() {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.loadServiceDetails(Number(idParam));
    } else {
      this.errorMessage = 'Serviço não encontrado.';
      this.isLoading = false;
    }
  }

  loadServiceDetails(id: number) {
    this.isLoading = true;
    this.serviceListing.getListingById(id).subscribe({
      next: (data) => {
        this.service = data;
        this.isLoading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.errorMessage = 'Não foi possível carregar os detalhes do serviço.';
        this.isLoading = false;
        this.cdr.markForCheck();
      }
    });
  }

  isHiring = false;

  hireService() {
    if (!this.service) return;
    
    this.isHiring = true;
    this.serviceListing.createCheckoutSession(this.service.id).subscribe({
      next: (response) => {
        // Redireciona o utilizador para a página segura do Stripe
        window.location.href = response.checkoutUrl;
      },
      error: (err) => {
        alert(err.error?.message || 'Erro ao iniciar pagamento. Tenta novamente.');
        this.isHiring = false;
        this.cdr.markForCheck();
      }
    });
  }
}
