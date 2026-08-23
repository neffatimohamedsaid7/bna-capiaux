import { HttpErrorResponse } from '@angular/common/http';
import { ErrorResponse } from './models/error-response.model';

export function extractErrorMessage(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    const body = err.error as Partial<ErrorResponse> | null;
    if (body?.message) {
      return body.message;
    }
    if (err.status === 0) {
      return 'Impossible de contacter le serveur backend.';
    }
    return `Erreur ${err.status} : ${err.statusText}`;
  }
  return 'Une erreur inattendue est survenue.';
}
