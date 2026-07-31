import { Component, Input, Output, EventEmitter, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TimeoutError } from 'rxjs';

import { ClientProfileService } from '../services/client-profile.service';

const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp'];
const MAX_SIZE_BYTES = 5242880; // 5 MB

@Component({
  selector: 'app-photo-upload',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="bg-white rounded-lg shadow p-6">
      <h2 class="text-lg font-semibold text-gray-800 mb-4">Foto de Perfil</h2>

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

      <div class="flex items-center gap-6">
        <!-- Current / Preview photo -->
        <div class="flex-shrink-0">
          @if (previewUrl() || photoUrl) {
            <img
              [src]="previewUrl() || photoUrl"
              alt="Foto de perfil"
              class="w-24 h-24 rounded-full object-cover border-2 border-gray-200"
            />
          } @else {
            <div class="w-24 h-24 rounded-full bg-gray-200 flex items-center justify-center">
              <svg class="w-10 h-10 text-gray-400" fill="currentColor" viewBox="0 0 24 24">
                <path d="M12 12c2.7 0 5-2.3 5-5s-2.3-5-5-5-5 2.3-5 5 2.3 5 5 5zm0 2c-3.3 0-10 1.7-10 5v3h20v-3c0-3.3-6.7-5-10-5z"/>
              </svg>
            </div>
          }
        </div>

        <!-- Upload controls -->
        <div class="flex flex-col gap-3">
          <label for="photo-input" class="text-sm font-medium text-gray-700">
            Seleccionar nueva foto
          </label>
          <input
            id="photo-input"
            type="file"
            accept="image/jpeg,image/png,image/webp"
            (change)="onFileSelected($event)"
            class="text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-md file:border-0 file:text-sm file:font-medium file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100"
            [attr.aria-describedby]="'photo-help'"
          />
          <p id="photo-help" class="text-xs text-gray-500">
            JPEG, PNG o WebP. Máximo 5 MB.
          </p>

          @if (selectedFile()) {
            <button
              (click)="onUpload()"
              [disabled]="uploading()"
              class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed w-fit"
            >
              @if (uploading()) {
                Subiendo...
              } @else {
                Subir Foto
              }
            </button>
          }
        </div>
      </div>
    </div>
  `,
})
export class PhotoUploadComponent {
  @Input() photoUrl: string | null = null;
  @Output() photoUpdated = new EventEmitter<string>();

  private readonly profileService: ClientProfileService;

  selectedFile = signal<File | null>(null);
  previewUrl = signal<string | null>(null);
  uploading = signal(false);
  notification = signal<{ type: 'success' | 'error'; message: string } | null>(null);

  constructor(profileService: ClientProfileService) {
    this.profileService = profileService;
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;

    this.selectedFile.set(null);
    this.previewUrl.set(null);
    this.notification.set(null);

    if (!file) return;

    // Validate type
    if (!ALLOWED_TYPES.includes(file.type)) {
      this.showNotification('error', 'Tipo de archivo no permitido. Solo se acepta JPEG, PNG o WebP.');
      input.value = '';
      return;
    }

    // Validate size
    if (file.size > MAX_SIZE_BYTES) {
      this.showNotification('error', 'El archivo excede el tamaño máximo de 5 MB.');
      input.value = '';
      return;
    }

    // Show preview
    this.selectedFile.set(file);
    this.previewUrl.set(URL.createObjectURL(file));
  }

  onUpload(): void {
    const file = this.selectedFile();
    if (!file || this.uploading()) return;

    this.uploading.set(true);
    this.notification.set(null);

    this.profileService.uploadPhoto(file).subscribe({
      next: (response) => {
        this.uploading.set(false);
        this.selectedFile.set(null);
        this.previewUrl.set(null);
        this.showNotification('success', 'Foto actualizada exitosamente.');
        this.photoUpdated.emit(response.photoUrl);
      },
      error: (err) => {
        this.uploading.set(false);
        const message = err instanceof TimeoutError
          ? 'La operación no pudo completarse por tiempo de espera agotado.'
          : err.error?.message ?? 'Error al subir la foto.';
        this.showNotification('error', message);
      },
    });
  }

  private showNotification(type: 'success' | 'error', message: string): void {
    this.notification.set({ type, message });
    setTimeout(() => this.notification.set(null), 3000);
  }
}
