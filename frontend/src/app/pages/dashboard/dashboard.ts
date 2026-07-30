import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { Auth } from '../../core/auth/auth';
import { CommonModule } from '@angular/common';
import { ServiceListingService, ServiceListing } from '../../core/services/service-listing';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink, CommonModule, FormsModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit {
  private auth = inject(Auth);
  private router = inject(Router);
  private serviceListing = inject(ServiceListingService);
  private cdr = inject(ChangeDetectorRef);

  allListings: ServiceListing[] = [];
  isLoading = false;
  selectedCategory = '';

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

  ngOnInit() {
    this.loadListings();
  }

  loadListings() {
    this.isLoading = true;
    this.serviceListing.getAllListings(this.selectedCategory).subscribe({
      next: (data) => {
        this.allListings = data;
        this.isLoading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Erro ao carregar anúncios:', err);
        this.isLoading = false;
        this.cdr.markForCheck();
      }
    });
  }

  onCategoryChange() {
    this.loadListings();
  }

  logout() {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
