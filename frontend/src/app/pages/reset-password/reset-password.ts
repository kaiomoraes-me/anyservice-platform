import { Component, inject, OnInit } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Auth } from '../../core/auth/auth';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-reset-password',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './reset-password.html',
  styleUrl: './reset-password.css',
})
export class ResetPassword implements OnInit {
  private fb = inject(FormBuilder);
  private auth = inject(Auth);
  private router = inject(Router);

  email = '';
  isLoading = false;

  resetForm = this.fb.group({
    code: ['', [Validators.required, Validators.pattern('^[0-9]{6}$')]],
    newPassword: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', [Validators.required]]
  }, { validators: this.passwordsMatchValidator });

  passwordsMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('newPassword')?.value;
    const confirmPassword = control.get('confirmPassword')?.value;
    return password === confirmPassword ? null : { passwordsMismatch: true };
  }

  ngOnInit() {
    const state = this.router.getCurrentNavigation()?.extras.state as { email: string };
    this.email = state?.email || history.state?.email;

    if (!this.email) {
      this.router.navigate(['/forgot-password']);
    }
  }

  onSubmit() {
    if (this.resetForm.valid) {
      this.isLoading = true;
      
      const payload = {
        email: this.email,
        code: this.resetForm.value.code!,
        newPassword: this.resetForm.value.newPassword!
      };

      this.auth.resetPassword(payload).subscribe({
        next: (res: any) => {
          this.isLoading = false;
          Swal.fire({
            title: 'Senha Redefinida!',
            text: 'Sua senha foi alterada com sucesso.',
            icon: 'success',
            background: '#1e1e2f',
            color: '#fff',
            confirmButtonColor: '#8b5cf6'
          }).then(() => {
            this.router.navigate(['/login']);
          });
        },
        error: (err) => {
          this.isLoading = false;
          Swal.fire({
            title: 'Ops!',
            text: err.error?.message || 'Código inválido ou expirado.',
            icon: 'error',
            background: '#1e1e2f',
            color: '#fff',
            confirmButtonColor: '#8b5cf6'
          });
        }
      });
    } else {
      this.resetForm.markAllAsTouched();
    }
  }
}
