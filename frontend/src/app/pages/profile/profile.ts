import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { UserService, UserProfile } from '../../core/user/user.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, RouterLink],
  templateUrl: './profile.html',
  styleUrl: './profile.css'
})
export class ProfileComponent implements OnInit, OnDestroy {
  profileForm: FormGroup;
  userProfile: UserProfile | null = null;
  isLoading = false;
  successMessage = '';
  errorMessage = '';

  // Phone verification modal
  showPhoneModal = false;
  phoneCode = '';
  phoneModalError = '';
  phoneModalSuccess = '';
  phoneModalLoading = false;
  countdown = 120;
  countdownDisplay = '2:00';
  canResend = false;
  private countdownInterval: any = null;

  // Phone form fields (separate from profile form)
  phoneCountryCode = '+55';
  phoneNumber = '';

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private cdr: ChangeDetectorRef
  ) {
    this.profileForm = this.fb.group({
      name: ['', Validators.required],
      username: [''],
      phoneVisible: [false],
      bio: ['']
    });
  }

  countries = [
    { code: '+55', flag: '🇧🇷', name: 'Brasil' },
    { code: '+1', flag: '🇺🇸', name: 'EUA/Canadá' },
    { code: '+351', flag: '🇵🇹', name: 'Portugal' },
    { code: '+44', flag: '🇬🇧', name: 'Reino Unido' },
    { code: '+34', flag: '🇪🇸', name: 'Espanha' },
    { code: '+54', flag: '🇦🇷', name: 'Argentina' }
  ];

  ngOnInit(): void {
    this.loadProfile();
  }

  ngOnDestroy(): void {
    this.clearCountdown();
  }

  loadProfile(): void {
    this.isLoading = true;
    this.userService.getProfile().subscribe({
      next: (profile) => {
        this.userProfile = profile;
        
        // Parse phone into country code and number for display
        let phone = profile.phone || '';
        let countryCode = '+55';
        
        if (phone.startsWith('+')) {
          const parts = phone.split(' ');
          if (parts.length > 1 && this.countries.find(c => c.code === parts[0])) {
            countryCode = parts[0];
            phone = parts.slice(1).join(' ');
          }
        }

        this.phoneCountryCode = countryCode;
        this.phoneNumber = phone;

        this.profileForm.patchValue({
          name: profile.name,
          username: profile.username,
          phoneVisible: profile.phoneVisible,
          bio: profile.bio
        });
        this.isLoading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.errorMessage = 'Erro ao carregar os dados do perfil.';
        this.isLoading = false;
        console.error(err);
        this.cdr.markForCheck();
      }
    });
  }

  onFileSelected(event: any): void {
    const file: File = event.target.files[0];
    if (file) {
      this.isLoading = true;
      this.userService.uploadAvatar(file).subscribe({
        next: (response) => {
          if (this.userProfile) {
            this.userProfile.profilePictureUrl = response.profilePictureUrl;
          }
          this.successMessage = 'Foto de perfil atualizada!';
          this.isLoading = false;
          this.cdr.markForCheck();
          setTimeout(() => {
            this.successMessage = '';
            this.cdr.markForCheck();
          }, 3000);
        },
        error: (err) => {
          this.errorMessage = 'Erro ao enviar a imagem.';
          this.isLoading = false;
          console.error(err);
          this.cdr.markForCheck();
        }
      });
    }
  }

  // ---- Phone Verification Flow ----

  sendPhoneCode(): void {
    if (!this.phoneNumber.trim()) {
      this.errorMessage = 'Introduza o número de telefone antes de verificar.';
      this.cdr.markForCheck();
      return;
    }

    this.phoneModalLoading = true;
    this.phoneModalError = '';
    this.phoneModalSuccess = '';
    this.phoneCode = '';
    this.showPhoneModal = true;
    this.cdr.markForCheck();

    this.userService.sendPhoneCode({
      countryCode: this.phoneCountryCode,
      phone: this.phoneNumber
    }).subscribe({
      next: () => {
        this.phoneModalLoading = false;
        this.startCountdown();
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.phoneModalLoading = false;
        this.phoneModalError = err.error?.message || err.error || 'Erro ao enviar o código SMS.';
        this.cdr.markForCheck();
      }
    });
  }

  resendPhoneCode(): void {
    if (!this.canResend) return;

    this.phoneModalLoading = true;
    this.phoneModalError = '';
    this.phoneModalSuccess = '';
    this.phoneCode = '';
    this.cdr.markForCheck();

    this.userService.sendPhoneCode({
      countryCode: this.phoneCountryCode,
      phone: this.phoneNumber
    }).subscribe({
      next: () => {
        this.phoneModalLoading = false;
        this.startCountdown();
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.phoneModalLoading = false;
        this.phoneModalError = err.error?.message || err.error || 'Erro ao reenviar o código SMS.';
        this.cdr.markForCheck();
      }
    });
  }

  verifyPhoneCode(): void {
    if (!this.phoneCode || this.phoneCode.length !== 6) {
      this.phoneModalError = 'O código deve ter 6 dígitos.';
      this.cdr.markForCheck();
      return;
    }

    this.phoneModalLoading = true;
    this.phoneModalError = '';
    this.cdr.markForCheck();

    this.userService.verifyPhoneCode({ code: this.phoneCode }).subscribe({
      next: () => {
        this.phoneModalLoading = false;
        this.phoneModalSuccess = 'Número verificado com sucesso!';
        this.cdr.markForCheck();

        // Fechar modal após 1.5s e recarregar perfil
        setTimeout(() => {
          this.closePhoneModal();
          this.loadProfile();
        }, 1500);
      },
      error: (err) => {
        this.phoneModalLoading = false;
        this.phoneModalError = err.error?.message || err.error || 'Código inválido ou expirado.';
        this.cdr.markForCheck();
      }
    });
  }

  closePhoneModal(): void {
    this.showPhoneModal = false;
    this.phoneCode = '';
    this.phoneModalError = '';
    this.phoneModalSuccess = '';
    this.clearCountdown();
    this.cdr.markForCheck();
  }

  private startCountdown(): void {
    this.clearCountdown();
    this.countdown = 120;
    this.canResend = false;
    this.updateCountdownDisplay();

    this.countdownInterval = setInterval(() => {
      this.countdown--;
      this.updateCountdownDisplay();
      
      if (this.countdown <= 0) {
        this.canResend = true;
        this.clearCountdown();
      }
      this.cdr.markForCheck();
    }, 1000);
  }

  private updateCountdownDisplay(): void {
    const minutes = Math.floor(this.countdown / 60);
    const seconds = this.countdown % 60;
    this.countdownDisplay = `${minutes}:${seconds.toString().padStart(2, '0')}`;
  }

  private clearCountdown(): void {
    if (this.countdownInterval) {
      clearInterval(this.countdownInterval);
      this.countdownInterval = null;
    }
  }

  // Validate phone based on country code
  get isPhoneValid(): boolean {
    if (!this.phoneNumber) return false;
    
    // Remove spaces and non-digits
    const cleanNumber = this.phoneNumber.replace(/\D/g, '');
    
    switch (this.phoneCountryCode) {
      case '+55': // Brasil: 10 or 11 digits
        return cleanNumber.length === 10 || cleanNumber.length === 11;
      case '+351': // Portugal: 9 digits
        return cleanNumber.length === 9;
      case '+1': // EUA/Canada: 10 digits
        return cleanNumber.length === 10;
      case '+44': // UK: 10 or 11 digits
        return cleanNumber.length === 10 || cleanNumber.length === 11;
      case '+34': // Espanha: 9 digits
        return cleanNumber.length === 9;
      case '+54': // Argentina: 10 digits
        return cleanNumber.length === 10;
      default:
        return cleanNumber.length >= 8 && cleanNumber.length <= 15;
    }
  }

  // Check if phone is already verified with the current number
  get isPhoneVerified(): boolean {
    if (!this.userProfile?.phoneVerified || !this.userProfile?.phone) return false;
    const currentFull = this.phoneCountryCode + ' ' + this.phoneNumber;
    return this.userProfile.phone === currentFull;
  }

  // ---- Profile Save ----

  onSubmit(): void {
    if (this.profileForm.invalid) {
      return;
    }

    this.isLoading = true;
    this.successMessage = '';
    this.errorMessage = '';

    const formValue = { ...this.profileForm.value };
    // Phone is NOT sent here anymore — it's managed via SMS verification

    this.userService.updateProfile(formValue).subscribe({
      next: (profile) => {
        this.userProfile = profile;
        this.successMessage = 'Perfil atualizado com sucesso!';
        this.isLoading = false;
        this.profileForm.markAsPristine();
        this.cdr.markForCheck();
        
        setTimeout(() => {
          this.successMessage = '';
          this.cdr.markForCheck();
        }, 3000);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || err.error || 'Erro ao atualizar o perfil. Verifique os dados e tente novamente.';
        this.isLoading = false;
        console.error(err);
        this.cdr.markForCheck();
      }
    });
  }
}
