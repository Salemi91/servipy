/**
 * Estructura uniforme de error devuelta por el backend.
 * Alineada con docs/API_CONTRACT.md.
 */
export interface ErrorResponse {
  timestamp: string;
  status: number;
  code: string;
  message: string;
  errors: FieldError[];
}

export interface FieldError {
  field: string;
  message: string;
}
