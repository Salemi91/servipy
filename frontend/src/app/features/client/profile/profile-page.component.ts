import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ClientProfileService } from '../services/client-profile.service';
import { ClientProfile } from '../models/client-profile.model';
import { ProfileEditFormComponent } from './profile-edit-form.component';
import { PhotoUploadComponent } from './photo-upload.component';
import { PasswordChangeComponent } from './password-change.component';

type ProfileState = 'loading' | 'loaded' | 'error';

@Component({
  selector: 'app-profile-page',
  standalone: true,
  imports: [
    CommonModule,
    ProfileEditFormComponent,
    PhotoUploadComponent,
    PasswordChangeComponent,
  ],
  template: `
    <!-- Loading State -->
    @if (state() === 'loading') {
      <div class="flex justify-center items-center py-16">
        <svg class="animate-spin h-8 w-8 text-blue-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"></path>
        </svg>
        <span class="ml-3 text-gray-600">Cargando perfil...</span>
      </div>
    }

    <!-- Error State -->
    @if (state() === 'error') {
      <div class="flex flex-col items-center py-16">
        <p class="text-red-600 mb-4">No se pudo cargar el perfil. Intente nuevamente.</p>
        <button
          (click)="loadProfile()"
          class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
        >
          Reintentar
        </button>
      </div>
    }

    <!-- Loaded State -->
    @if (state() === 'loaded' && profile()) {
      <div class="space-y-6">
        <h1 class="text-2xl font-semibold text-gray-800">Mi Perfil</h1>

        <app-photo-upload
          [photoUrl]="profile()!.photoUrl"
          (photoUpdated)="onPhotoUpdated($event)"
        />

        <app-profile-edit-form
          [profile]="profile()!"
          (profileUpdated)="onProfileUpdated($event)"
        />

        <app-password-change />
      </div>
    }
  `,
})
export class ProfilePageComponent implements OnInit {
  private readonly profileService: ClientProfileService;

  state = signal<ProfileState>('loading');
  profile = signal<ClientProfile | null>(null);

  constructor(profileService: ClientProfileService) {
    this.profileService = profileService;
  }

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.state.set('loading');
    this.profileService.getProfile().subscribe({
      next: (data) => {
        this.profile.set(data);
        this.state.set('loaded');
      },
      error: () => {
        this.state.set('error');
      },
    });
  }

  onProfileUpdated(updatedProfile: ClientProfile): void {
    this.profile.set(updatedProfile);
  }

  onPhotoUpdated(photoUrl: string): void {
    const current = this.profile();
    if (current) {
      this.profile.set({ ...current, photoUrl });
    }
  }
}
