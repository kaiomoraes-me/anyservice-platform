import { Component, inject, OnInit } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Auth } from '../../core/auth/auth';

@Component({
  selector: 'app-verify-account',
  imports: [ReactiveFormsModule],
  templateUrl: './verify-account.html',
  styleUrl: './verify-account.css',
})
export class VerifyAccount implements OnInit {
  private fb = inject(FormBuilder);
  private auth = inject(Auth);
  private router = inject(Router);

  email = '';
  errorMessage = '';
  successMessage = '';
  isLoading = false;

  verifyForm = this.fb.group({
    code: ['', [Validators.required, Validators.pattern('^[0-9]{6}$')]]
  });

  ngOnInit() {
    // Pega o email que foi passado pelo Router State
    const navigation = this.router.getCurrentNavigation();
    const state = navigation?.extras.state as { email: string };
    
    // Se o state não existir, tenta pegar do history
    this.email = state?.email || history.state?.email;

    if (!this.email) {
      this.router.navigate(['/login']); // Redireciona se não houver e-mail
    }
  }

  onSubmit() {
    if (this.verifyForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';
      
      const payload = {
        email: this.email,
        code: this.verifyForm.value.code!
      };

      this.auth.verifyAccount(payload).subscribe({
        next: (res: any) => {
          this.isLoading = false;
          this.successMessage = 'Conta ativada com sucesso! Redirecionando...';
          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 2000);
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMessage = err.error?.message || 'Código inválido ou expirado.';
        }
      });
    } else {
      this.verifyForm.markAllAsTouched();
    }
  }
}
