import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TimeoutError } from 'rxjs';

import { ClientProfileService } from '../services/client-profile.service';
import { ClientProfile, ProfileUpdateRequest } from '../models/client-profile.model';

@Component({
  selector: 'app-profile-edit-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="bg-white rounded-lg shadow p-6">
      <h2 class="text-lg font-semibold text-gray-800 mb-4">Datos Personales</h2>

      <!-- Notification -->
      @if (notification()) {
        <div
          [attr.role]="notification()!.type === 'success' ? 'status' : 'alert'"
          [class]="notification()!.type === 'success'
            ? 'text-sm text-green-600 bg-green-50 border border-green-200 rounded-md p-3 mb-4'
            : 'text-sm text-red-600 bg-red-50 border border-red-200 rounded-md p-3 mb-4'"
        >
          {{ notification()!.message }}
        </div>
      }

      <form [formGroup]="form" (ngSubmit)="onSubmit()">
        <!-- Name field -->
        <div class="mb-4">
          <label for="name" class="block text-sm font-medium text-gray-700 mb-1">Nombre</label>
          <input
            id="name"
            type="text"
            formControlName="name"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            [attr.aria-invalid]="isFieldInvalid('name')"
            aria-describedby="name-error"
          />
          @if (isFieldInvalid('name')) {
            <p id="name-error" class="text-sm text-red-600 mt-1">
              @if (form.get('name')!.hasError('required')) {
                El nombre es requerido.
              } @else if (form.get('name')!.hasError('minlength')) {
                El nombre debe tener al menos 2 caracteres.
              } @else if (form.get('name')!.hasError('maxlength')) {
                El nombre no puede exceder 100 caracteres.
              }
            </p>
          }
        </div>

        <!-- Email (read-only) -->
        <div class="mb-4">
          <label for="email" class="block text-sm font-medium text-gray-700 mb-1">Email</label>
          <input
            id="email"
            type="email"
            [value]="profile.email"
            disabled
            class="w-full px-3 py-2 border border-gray-200 rounded-md bg-gray-50 text-gray-500 cursor-not-allowed"
          />
        </div>

        <!-- Phone field -->
        <div class="mb-4">
          <label for="phone" class="block text-sm font-medium text-gray-700 mb-1">Teléfono</label>
          <input
            id="phone"
            type="tel"
            formControlName="phone"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            [attr.aria-invalid]="isFieldInvalid('phone')"
            aria-describedby="phone-error"
          />
          @if (isFieldInvalid('phone')) {
            <p id="phone-error" class="text-sm text-red-600 mt-1">
              @if (form.get('phone')!.hasError('maxlength')) {
                El teléfono no puede exceder 20 caracteres.
              } @else if (form.get('phone')!.hasError('pattern')) {
                Solo se permiten dígitos, espacios, guiones y el prefijo +.
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
            Guardando...
          } @else {
            Guardar Cambios
          }
        </button>
      </form>
    </div>
  `,
})
export class ProfileEditFormComponent implements OnChanges {
  @Input({ required: true }) profile!: ClientProfile;
  @Output() profileUpdated = new EventEmitter<ClientProfile>();

  private readonly fb: FormBuilder;
  private readonly profileService: ClientProfileService;

  submitting = signal(false);
  notification = signal<{ type: 'success' | 'error'; message: string } | null>(null);

  form: FormGroup;

  constructor(fb: FormBuilder, profileService: ClientProfileService) {
    this.fb = fb;
    this.profileService = profileService;
    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      phone: ['', [Validators.maxLength(20), Validators.pattern(/^[\d\s\-+]*$/)]],
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['profile'] && this.profile) {
      this.form.patchValue({
        name: this.profile.name,
        phone: this.profile.phone ?? '',
      });
    }
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.form.get(fieldName);
    return !!field && field.invalid && (field.dirty || field.touched);
  }

  onSubmit(): void {
    if (this.form.invalid || this.submitting()) return;

    this.submitting.set(true);
    this.notification.set(null);

    const request: ProfileUpdateRequest = {
      name: this.form.value.name.trim(),
      phone: this.form.value.phone?.trim() || null,
    };

    this.profileService.updateProfile(request).subscribe({
      next: (updatedProfile) => {
        this.submitting.set(false);
        this.showNotification('success', 'Perfil actualizado exitosamente.');
        this.profileUpdated.emit(updatedProfile);
      },
      error: (err) => {
        this.submitting.set(false);
        const message = err instanceof TimeoutError
          ? 'La operación no pudo completarse por tiempo de espera agotado.'
          : err.error?.message ?? 'Error al actualizar el perfil.';
        this.showNotification('error', message);
      },
    });
  }

  private showNotification(type: 'success' | 'error', message: string): void {
    this.notification.set({ type, message });
    setTimeout(() => this.notification.set(null), 3000);
  }
}
