import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { Auth } from '../auth/auth';

export const providerGuard = () => {
  const auth = inject(Auth);
  const router = inject(Router);
  
  if (auth.isLoggedIn()) {
    const user = auth.getUser();
    if (user && user.role === 'PROVIDER') {
      return true;
    } else {
      router.navigate(['/dashboard']);
      return false;
    }
  }

  router.navigate(['/login']);
  return false;
};
