import { Component, inject } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Auth } from '../../core/auth/auth';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-forgot-password',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.css',
})
export class ForgotPassword {
  private fb = inject(FormBuilder);
  private auth = inject(Auth);
  private router = inject(Router);

  forgotForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]]
  });

  isLoading = false;

  onSubmit() {
    if (this.forgotForm.valid) {
      this.isLoading = true;
      const email = this.forgotForm.value.email!;

      this.auth.forgotPassword(email).subscribe({
        next: (res: any) => {
          this.isLoading = false;
          Swal.fire({
            title: 'Código Enviado!',
            text: 'Verifique seu e-mail para redefinir a senha.',
            icon: 'success',
            background: '#1e1e2f',
            color: '#fff',
            confirmButtonColor: '#8b5cf6'
          }).then(() => {
            this.router.navigate(['/reset-password'], { state: { email: email } });
          });
        },
        error: (err) => {
          this.isLoading = false;
          Swal.fire({
            title: 'Ops!',
            text: err.status === 400 || err.status === 500 
                  ? 'Não foi possível enviar o código. Verifique se o e-mail está correto.' 
                  : 'Ocorreu um erro no servidor.',
            icon: 'error',
            background: '#1e1e2f',
            color: '#fff',
            confirmButtonColor: '#8b5cf6'
          });
        }
      });
    } else {
      this.forgotForm.markAllAsTouched();
    }
  }
}
