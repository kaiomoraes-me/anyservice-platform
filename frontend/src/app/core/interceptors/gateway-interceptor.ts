import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { throwError, catchError } from 'rxjs';
import Swal from 'sweetalert2';

export const gatewayInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // Catch Network Partitioning / Gateway timeouts (503/504) or Internal Server Error (500)
      if (error.status === 500 || error.status === 503 || error.status === 504 || error.status === 0) {
        // Fallback UI Monochrome
        Swal.fire({
          title: 'Serviço Temporariamente Indisponível',
          text: 'Ocorreu um erro interno no servidor ou o serviço está offline. Por favor, tente novamente mais tarde.',
          icon: 'error',
          confirmButtonColor: '#000000', // STRICT BLACK
          background: '#FFFFFF', // STRICT WHITE
          color: '#000000',
          confirmButtonText: 'Entendi'
        });
      }
      return throwError(() => error);
    })
  );
};
