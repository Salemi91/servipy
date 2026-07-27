import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TimeoutError } from 'rxjs';

import { ClientProfileService } from '../services/client-profile.service';

@Component({
  selector: 'app-password-change',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="bg-white rounded-lg shadow p-6">
      <h2 class="text-lg font-semibold text-gray-800 mb-4">Cambiar Contraseña</h2>

      <!-- Notification -->
      @if (notification()) {
        <div
          [class]="notification()!.type === 'success'
            ? 'text-sm text-green-600 bg-green-50 border border-green-200 rounded-md p-3 mb-4'
            : 'text-sm text-red-600 bg-red-50 border border-red-200 rounded-md p-3 mb-4'"
        >
          {{ notification()!.message }}
        </div>
      }

      <form [formGroup]="form" (ngSubmit)="onSubmit()">
        <!-- Current password -->
        <div class="mb-4">
          <label for="currentPassword" class="block text-sm font-medium text-gray-700 mb-1">
            Contraseña Actual
          </label>
          <input
            id="currentPassword"
            type="password"
            formControlName="currentPassword"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            [attr.aria-invalid]="isFieldInvalid('currentPassword')"
            aria-describedby="currentPassword-error"
          />
          @if (isFieldInvalid('currentPassword')) {
            <p id="currentPassword-error" class="text-sm text-red-600 mt-1">
              La contraseña actual es requerida.
            </p>
          }
        </div>

        <!-- New password -->
        <div class="mb-4">
          <label for="newPassword" class="block text-sm font-medium text-gray-700 mb-1">
            Nueva Contraseña
          </label>
          <input
            id="newPassword"
            type="password"
            formControlName="newPassword"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            [attr.aria-invalid]="isFieldInvalid('newPassword')"
            aria-describedby="newPassword-error newPassword-help"
          />
          <p id="newPassword-help" class="text-xs text-gray-500 mt-1">
            Entre 8 y 72 caracteres.
          </p>
          @if (isFieldInvalid('newPassword')) {
            <p id="newPassword-error" class="text-sm text-red-600 mt-1">
              @if (form.get('newPassword')!.hasError('required')) {
                La nueva contraseña es requerida.
              } @else if (form.get('newPassword')!.hasError('minlength')) {
                La contraseña debe tener al menos 8 caracteres.
              } @else if (form.get('newPassword')!.hasError('maxlength')) {
                La contraseña no puede exceder 72 caracteres.
              }
            </p>
          }
        </div>

        <!-- Submit button -->
        <button
          type="submit"
          [disabled]="submitting() || form.invalid"
          class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          @if (submitting()) {
            Cambiando...
          } @else {
            Cambiar Contraseña
          }
        </button>
      </form>
    </div>
  `,
})
export class PasswordChangeComponent {
  private readonly fb: FormBuilder;
  private readonly profileService: ClientProfileService;

  submitting = signal(false);
  notification = signal<{ type: 'success' | 'error'; message: string } | null>(null);

  form: FormGroup;

  constructor(fb: FormBuilder, profileService: ClientProfileService) {
    this.fb = fb;
    this.profileService = profileService;
    this.form = this.fb.group({
      currentPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
    });
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.form.get(fieldName);
    return !!field && field.invalid && (field.dirty || field.touched);
  }

  onSubmit(): void {
    if (this.form.invalid || this.submitting()) return;

    this.submitting.set(true);
    this.notification.set(null);

    const request = {
      currentPassword: this.form.value.currentPassword,
      newPassword: this.form.value.newPassword,
    };

    this.profileService.changePassword(request).subscribe({
      next: () => {
        this.submitting.set(false);
        this.form.reset();
        this.showNotification('success', 'Contraseña actualizada exitosamente.');
      },
      error: (err) => {
        this.submitting.set(false);
        const message = err instanceof TimeoutError
          ? 'La operación no pudo completarse por tiempo de espera agotado.'
          : err.error?.message ?? 'Error al cambiar la contraseña.';
        this.showNotification('error', message);
      },
    });
  }

  private showNotification(type: 'success' | 'error', message: string): void {
    this.notification.set({ type, message });
    setTimeout(() => this.notification.set(null), 3000);
  }
}
