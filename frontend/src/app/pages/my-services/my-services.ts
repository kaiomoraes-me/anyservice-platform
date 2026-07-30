import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ServiceListingService, ServiceListing } from '../../core/services/service-listing';

@Component({
  selector: 'app-my-services',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './my-services.html',
  styleUrl: './my-services.css'
})
export class MyServicesComponent implements OnInit {
  myListings: ServiceListing[] = [];
  serviceForm: FormGroup;
  
  isLoading = false;
  isSubmitting = false;
  showCreateModal = false;
  
  errorMessage = '';
  successMessage = '';

  categories = [
    'Tecnologia & Programação',
    'Design & Multimédia',
    'Limpeza Doméstica',
    'Obras & Reparações',
    'Aulas & Explicações',
    'Beleza & Estética',
    'Saúde & Bem-estar',
    'Outros'
  ];

  constructor(
    private serviceListing: ServiceListingService,
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef
  ) {
    this.serviceForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(100)]],
      description: ['', [Validators.required, Validators.maxLength(1000)]],
      price: ['', [Validators.required, Validators.min(1)]],
      category: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadMyServices();
  }

  loadMyServices(): void {
    this.isLoading = true;
    this.serviceListing.getMyListings().subscribe({
      next: (data) => {
        this.myListings = data;
        this.isLoading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.errorMessage = 'Erro ao carregar os teus serviços.';
        this.isLoading = false;
        this.cdr.markForCheck();
      }
    });
  }

  openCreateModal(): void {
    this.serviceForm.reset();
    this.showCreateModal = true;
    this.errorMessage = '';
    this.cdr.markForCheck();
  }

  closeCreateModal(): void {
    this.showCreateModal = false;
    this.cdr.markForCheck();
  }

  onSubmit(): void {
    if (this.serviceForm.invalid) {
      this.serviceForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';

    this.serviceListing.createListing(this.serviceForm.value).subscribe({
      next: (newListing) => {
        this.myListings.unshift(newListing);
        this.successMessage = 'Serviço criado com sucesso!';
        this.isSubmitting = false;
        this.closeCreateModal();
        this.cdr.markForCheck();

        setTimeout(() => {
          this.successMessage = '';
          this.cdr.markForCheck();
        }, 3000);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Erro ao criar o serviço. Tenta novamente.';
        this.isSubmitting = false;
        this.cdr.markForCheck();
      }
    });
  }

  deleteListing(id: number): void {
    if (confirm('Tens a certeza que queres apagar este serviço? Esta ação não pode ser desfeita.')) {
      this.serviceListing.deleteListing(id).subscribe({
        next: () => {
          this.myListings = this.myListings.filter(s => s.id !== id);
          this.cdr.markForCheck();
        },
        error: (err) => {
          alert('Erro ao apagar o serviço.');
        }
      });
    }
  }
}
