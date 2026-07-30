import { Routes } from '@angular/router';
import { Login } from './pages/login/login';
import { Register } from './pages/register/register';
import { Dashboard } from './pages/dashboard/dashboard';
import { VerifyAccount } from './pages/verify-account/verify-account';
import { ForgotPassword } from './pages/forgot-password/forgot-password';
import { ResetPassword } from './pages/reset-password/reset-password';
import { ProfileComponent } from './pages/profile/profile';
import { MyServicesComponent } from './pages/my-services/my-services';
import { ServiceDetailsComponent } from './pages/service-details/service-details';
import { PaymentSuccessComponent } from './pages/payment-success/payment-success';
import { PaymentCancelComponent } from './pages/payment-cancel/payment-cancel';
import { MyOrdersComponent } from './pages/my-orders/my-orders';
import { ChatComponent } from './pages/chat/chat';
import { authGuard } from './core/guards/auth-guard';
import { guestGuard } from './core/guards/guest-guard';
import { providerGuard } from './core/guards/provider-guard';

export const routes: Routes = [
    { path: '', redirectTo: 'login', pathMatch: 'full' },
    { path: 'login', component: Login, canActivate: [guestGuard] },
    { path: 'register', component: Register, canActivate: [guestGuard] },
    { path: 'verify-account', component: VerifyAccount },
    { path: 'forgot-password', component: ForgotPassword },
    { path: 'reset-password', component: ResetPassword },
    { path: 'dashboard', component: Dashboard, canActivate: [authGuard] },
    { path: 'profile', component: ProfileComponent, canActivate: [authGuard] },
    { path: 'my-services', component: MyServicesComponent, canActivate: [providerGuard] },
    { path: 'services/:id', component: ServiceDetailsComponent, canActivate: [authGuard] },
    { path: 'payment/success', component: PaymentSuccessComponent, canActivate: [authGuard] },
    { path: 'payment/cancel', component: PaymentCancelComponent, canActivate: [authGuard] },
    { path: 'my-orders', component: MyOrdersComponent, canActivate: [authGuard] },
    { path: 'chat/:orderId', component: ChatComponent, canActivate: [authGuard] },
    { path: '**', redirectTo: 'login' } // Rota curinga (qualquer URL inválida vai pro login)
];
