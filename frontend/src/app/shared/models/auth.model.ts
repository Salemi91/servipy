/**
 * Modelos de autenticación alineados con el contrato API del backend.
 * Endpoints: POST /api/v1/auth/login, POST /api/v1/auth/register/client,
 *            POST /api/v1/auth/register/professional, GET /api/v1/auth/me
 */

export type Role = 'CLIENT' | 'PROFESSIONAL' | 'ADMIN';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}

export interface UserResponse {
  id: number;
  name: string;
  email: string;
  role: Role;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  user: UserResponse;
}
