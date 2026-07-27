import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { Role } from '../../../shared/models/auth.model';

/**
 * Página de inicio de sesión.
 * Formulario reactivo con email y password, manejo de errores y redirección según rol.
 */
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="flex min-h-[60vh] items-center justify-center px-4">
      <div class="w-full max-w-md space-y-6">
        <div class="text-center">
          <img src="/images/logo.png" alt="ServiPy" class="mx-auto h-32 w-auto" />
          <h1 class="mt-4 text-2xl font-bold text-gray-900">Iniciar sesión</h1>
          <p class="mt-1 text-sm text-gray-500">Ingresá tus credenciales para acceder</p>
        </div>

        <form [formGroup]="form" (ngSubmit)="onSubmit()" class="space-y-4">
          <!-- Error general -->
          @if (errorMessage()) {
            <div role="alert" class="rounded-md bg-red-50 p-3 text-sm text-red-700">
              {{ errorMessage() }}
            </div>
          }

          <!-- Email -->
          <div>
            <label for="email" class="block text-sm font-medium text-gray-700">Email</label>
            <input
              id="email"
              type="email"
              formControlName="email"
              autocomplete="email"
              class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
              [class.border-red-500]="form.get('email')?.invalid && form.get('email')?.touched"
            />
            @if (form.get('email')?.invalid && form.get('email')?.touched) {
              <p class="mt-1 text-xs text-red-600" role="alert">
                Ingresá un email válido
              </p>
            }
          </div>

          <!-- Password -->
          <div>
            <label for="password" class="block text-sm font-medium text-gray-700">Contraseña</label>
            <input
              id="password"
              type="password"
              formControlName="password"
              autocomplete="current-password"
              class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
              [class.border-red-500]="form.get('password')?.invalid && form.get('password')?.touched"
            />
            @if (form.get('password')?.invalid && form.get('password')?.touched) {
              <p class="mt-1 text-xs text-red-600" role="alert">
                La contraseña es obligatoria
              </p>
            }
          </div>

          <!-- Submit -->
          <button
            type="submit"
            [disabled]="form.invalid || loading()"
            class="w-full rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
          >
            @if (loading()) {
              <span class="inline-flex items-center gap-2">
                <svg class="h-4 w-4 animate-spin" viewBox="0 0 24 24" fill="none">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"></path>
                </svg>
                Ingresando...
              </span>
            } @else {
              Iniciar sesión
            }
          </button>
        </form>

        <!-- Link a registro -->
        <p class="text-center text-sm text-gray-500">
          ¿No tenés cuenta?
          <a routerLink="/register" class="font-medium text-indigo-600 hover:text-indigo-500">
            Registrate
          </a>
        </p>
      </div>
    </div>
  `,
})
export class LoginComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
  });

  onSubmit(): void {
    if (this.form.invalid) return;

    this.loading.set(true);
    this.errorMessage.set(null);

    const { email, password } = this.form.getRawValue();

    this.authService.login(email, password).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.redirectByRole(res.user.role);
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('Email o contraseña incorrectos');
      },
    });
  }

  private redirectByRole(role: Role): void {
    const routes: Record<Role, string> = {
      CLIENT: '/client',
      PROFESSIONAL: '/professional',
      ADMIN: '/admin',
    };
    this.router.navigate([routes[role]]);
  }
}
