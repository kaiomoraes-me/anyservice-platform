import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { Auth } from '../auth/auth';
import { catchError, throwError } from 'rxjs';
import Swal from 'sweetalert2';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(Auth);
  const router = inject(Router);
  const token = auth.getToken();

  let requestToForward = req;

  if (token) {
    requestToForward = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(requestToForward).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 || error.status === 403) {
        auth.logout();
        Swal.fire({
          icon: 'warning',
          title: 'Sessão Expirada',
          text: 'A sua sessão expirou por segurança. Por favor, inicie sessão novamente.',
          confirmButtonColor: '#2563eb'
        });
        router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};
