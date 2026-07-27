import { Injectable, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { ApiService } from '../http/api.service';
import {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  Role,
  UserResponse,
} from '../../shared/models/auth.model';

const TOKEN_KEY = 'access_token';

/**
 * Servicio singleton de autenticación.
 * Gestiona login, registro, logout, estado del usuario y token JWT.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);

  /** Signal con los datos del usuario autenticado o null. */
  readonly currentUser = signal<UserResponse | null>(null);

  /**
   * Inicia sesión con email y password.
   * Almacena el token y actualiza el signal del usuario.
   */
  login(email: string, password: string): Observable<AuthResponse> {
    const body: LoginRequest = { email, password };
    return this.api.post<AuthResponse>('/auth/login', body).pipe(
      tap((res) => this.handleAuthSuccess(res))
    );
  }

  /**
   * Registra un nuevo usuario como CLIENT o PROFESSIONAL.
   * Almacena el token y actualiza el signal del usuario.
   */
  register(
    name: string,
    email: string,
    password: string,
    roleType: 'client' | 'professional'
  ): Observable<AuthResponse> {
    const body: RegisterRequest = { name, email, password };
    return this.api.post<AuthResponse>(`/auth/register/${roleType}`, body).pipe(
      tap((res) => this.handleAuthSuccess(res))
    );
  }

  /**
   * Cierra la sesión: elimina token, limpia estado y redirige a /login.
   */
  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }

  /**
   * Retorna el token almacenado o null si no existe.
   */
  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  /**
   * Indica si hay un token almacenado en localStorage.
   */
  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  /**
   * Decodifica el role del JWT almacenado.
   * Retorna null si no hay token o el payload es inválido.
   */
  getUserRole(): Role | null {
    const token = this.getToken();
    if (!token) return null;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.role ?? null;
    } catch {
      return null;
    }
  }

  /**
   * Restaura la sesión desde localStorage al iniciar la app.
   * Decodifica el JWT para extraer los datos del usuario.
   */
  restoreSession(): void {
    const token = this.getToken();
    if (!token) return;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      if (payload.sub && payload.email && payload.role) {
        this.currentUser.set({
          id: Number(payload.sub),
          name: payload.name ?? payload.email,
          email: payload.email,
          role: payload.role,
        });
      }
    } catch {
      // Token inválido — limpiar
      localStorage.removeItem(TOKEN_KEY);
      this.currentUser.set(null);
    }
  }

  private handleAuthSuccess(res: AuthResponse): void {
    localStorage.setItem(TOKEN_KEY, res.accessToken);
    this.currentUser.set(res.user);
  }
}
