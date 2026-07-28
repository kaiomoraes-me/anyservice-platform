import { Component, inject, ChangeDetectorRef } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Auth } from '../../core/auth/auth';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {
  private fb = inject(FormBuilder);
  private auth = inject(Auth);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  registerForm = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(3)]],
    username: ['', [Validators.required, Validators.pattern('^[a-z0-9_]+$')]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', [Validators.required]],
    role: ['CLIENT', Validators.required]
  }, { validators: this.passwordsMatchValidator });

  errorMessage = '';
  successMessage = '';
  isLoading = false;

  passwordsMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('password')?.value;
    const confirmPassword = control.get('confirmPassword')?.value;
    return password === confirmPassword ? null : { passwordsMismatch: true };
  }

  onSubmit() {
    if (this.registerForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';
      this.successMessage = '';
      
      const { confirmPassword, ...userData } = this.registerForm.value;

      this.auth.register(userData).subscribe({
        next: (res: any) => {
          this.isLoading = false;
          this.cdr.markForCheck();
          Swal.fire({
            title: 'Conta Criada!',
            text: 'Enviamos um código para o seu e-mail.',
            icon: 'success',
            background: '#1e1e2f',
            color: '#fff',
            confirmButtonColor: '#8b5cf6'
          }).then(() => {
            this.router.navigate(['/verify-account'], { state: { email: userData.email } });
          });
        },
        error: (err) => {
          this.isLoading = false;
          this.cdr.markForCheck();
          
          let errorMsg = 'Ocorreu um erro no servidor. Verifique o console ou as credenciais de e-mail.';
          
          if (err.status === 400) {
            errorMsg = err.error?.message || err.error || 'Dados inválidos. Verifique o e-mail ou o nome de usuário.';
          } else if (err.status === 500) {
             // Pode ser falha de envio de email
            errorMsg = 'Erro interno (500). Provavelmente as credenciais de e-mail do Gmail no .env estão incorretas!';
          }

          Swal.fire({
            title: 'Ops!',
            text: errorMsg,
            icon: 'error',
            background: '#1e1e2f',
            color: '#fff',
            confirmButtonColor: '#8b5cf6'
          });
        }
      });
    } else {
      this.registerForm.markAllAsTouched();
    }
  }
}
