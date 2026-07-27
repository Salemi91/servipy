import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../../core/auth/auth.service';
import { Role } from '../../../shared/models/auth.model';
import { ErrorResponse } from '../../../shared/models/api-response.model';

/**
 * Página de registro de usuario.
 * Formulario reactivo con name, email, password y selector de tipo de cuenta.
 */
@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="flex min-h-[60vh] items-center justify-center px-4">
      <div class="w-full max-w-md space-y-6">
        <div class="text-center">
          <h1 class="text-2xl font-bold text-gray-900">Crear cuenta</h1>
          <p class="mt-1 text-sm text-gray-500">Registrate para comenzar a usar ServiPy</p>
        </div>

        <form [formGroup]="form" (ngSubmit)="onSubmit()" class="space-y-4">
          <!-- Error general -->
          @if (errorMessage()) {
            <div role="alert" class="rounded-md bg-red-50 p-3 text-sm text-red-700">
              {{ errorMessage() }}
            </div>
          }

          <!-- Nombre -->
          <div>
            <label for="name" class="block text-sm font-medium text-gray-700">Nombre</label>
            <input
              id="name"
              type="text"
              formControlName="name"
              autocomplete="name"
              class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
              [class.border-red-500]="form.get('name')?.invalid && form.get('name')?.touched"
            />
            @if (form.get('name')?.hasError('required') && form.get('name')?.touched) {
              <p class="mt-1 text-xs text-red-600" role="alert">El nombre es obligatorio</p>
            }
            @if (form.get('name')?.hasError('minlength') && form.get('name')?.touched) {
              <p class="mt-1 text-xs text-red-600" role="alert">El nombre debe tener al menos 2 caracteres</p>
            }
            @if (fieldError('name'); as msg) {
              <p class="mt-1 text-xs text-red-600" role="alert">{{ msg }}</p>
            }
          </div>

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
              <p class="mt-1 text-xs text-red-600" role="alert">Ingresá un email válido</p>
            }
            @if (fieldError('email'); as msg) {
              <p class="mt-1 text-xs text-red-600" role="alert">{{ msg }}</p>
            }
          </div>

          <!-- Password -->
          <div>
            <label for="password" class="block text-sm font-medium text-gray-700">Contraseña</label>
            <input
              id="password"
              type="password"
              formControlName="password"
              autocomplete="new-password"
              class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
              [class.border-red-500]="form.get('password')?.invalid && form.get('password')?.touched"
            />
            @if (form.get('password')?.hasError('required') && form.get('password')?.touched) {
              <p class="mt-1 text-xs text-red-600" role="alert">La contraseña es obligatoria</p>
            }
            @if (form.get('password')?.hasError('minlength') && form.get('password')?.touched) {
              <p class="mt-1 text-xs text-red-600" role="alert">La contraseña debe tener al menos 8 caracteres</p>
            }
            @if (fieldError('password'); as msg) {
              <p class="mt-1 text-xs text-red-600" role="alert">{{ msg }}</p>
            }
          </div>

          <!-- Tipo de cuenta -->
          <div>
            <label class="block text-sm font-medium text-gray-700">Tipo de cuenta</label>
            <div class="mt-2 grid grid-cols-2 gap-3">
              <button
                type="button"
                (click)="form.get('roleType')?.setValue('client')"
                class="rounded-md border px-4 py-2 text-sm font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2"
                [class.border-indigo-600]="form.get('roleType')?.value === 'client'"
                [class.bg-indigo-50]="form.get('roleType')?.value === 'client'"
                [class.text-indigo-700]="form.get('roleType')?.value === 'client'"
                [class.border-gray-300]="form.get('roleType')?.value !== 'client'"
                [class.text-gray-700]="form.get('roleType')?.value !== 'client'"
              >
                Cliente
              </button>
              <button
                type="button"
                (click)="form.get('roleType')?.setValue('professional')"
                class="rounded-md border px-4 py-2 text-sm font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2"
                [class.border-indigo-600]="form.get('roleType')?.value === 'professional'"
                [class.bg-indigo-50]="form.get('roleType')?.value === 'professional'"
                [class.text-indigo-700]="form.get('roleType')?.value === 'professional'"
                [class.border-gray-300]="form.get('roleType')?.value !== 'professional'"
                [class.text-gray-700]="form.get('roleType')?.value !== 'professional'"
              >
                Profesional
              </button>
            </div>
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
                Registrando...
              </span>
            } @else {
              Crear cuenta
            }
          </button>
        </form>

        <!-- Link a login -->
        <p class="text-center text-sm text-gray-500">
          ¿Ya tenés cuenta?
          <a routerLink="/login" class="font-medium text-indigo-600 hover:text-indigo-500">
            Iniciá sesión
          </a>
        </p>
      </div>
    </div>
  `,
})
export class RegisterComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly fieldErrors = signal<Record<string, string>>({});

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    roleType: ['client' as 'client' | 'professional'],
  });

  fieldError(field: string): string | null {
    return this.fieldErrors()[field] ?? null;
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    this.loading.set(true);
    this.errorMessage.set(null);
    this.fieldErrors.set({});

    const { name, email, password, roleType } = this.form.getRawValue();

    this.authService.register(name, email, password, roleType).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.redirectByRole(res.user.role);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        this.handleError(err);
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

  private handleError(err: HttpErrorResponse): void {
    const body = err.error as ErrorResponse | undefined;

    if (err.status === 409) {
      this.errorMessage.set('El email ya está registrado');
      return;
    }

    if (err.status === 400 && body?.errors?.length) {
      const errors: Record<string, string> = {};
      for (const fieldErr of body.errors) {
        errors[fieldErr.field] = fieldErr.message;
      }
      this.fieldErrors.set(errors);
      return;
    }

    this.errorMessage.set('Ocurrió un error al registrar. Intentá nuevamente.');
  }
}
