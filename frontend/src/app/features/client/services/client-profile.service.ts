import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { timeout } from 'rxjs';
import { ApiService } from '../../../core/http/api.service';
import { API_BASE_URL } from '../../../core/config/api.config';
import { ClientProfile, ProfileUpdateRequest, PasswordChangeRequest, PhotoUploadResponse } from '../models/client-profile.model';

@Injectable({ providedIn: 'root' })
export class ClientProfileService {
  private readonly api = inject(ApiService);
  private readonly http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);

  private readonly TIMEOUT_MS = 15000;

  getProfile(): Observable<ClientProfile> {
    return this.api.get<ClientProfile>('/client/profile').pipe(timeout(this.TIMEOUT_MS));
  }

  updateProfile(data: ProfileUpdateRequest): Observable<ClientProfile> {
    return this.api.put<ClientProfile>('/client/profile', data).pipe(timeout(this.TIMEOUT_MS));
  }

  uploadPhoto(file: File): Observable<PhotoUploadResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.put<PhotoUploadResponse>(
      `${this.baseUrl}/client/profile/photo`, formData
    ).pipe(timeout(this.TIMEOUT_MS));
  }

  changePassword(data: PasswordChangeRequest): Observable<{ message: string }> {
    return this.api.put<{ message: string }>(
      '/client/profile/password', data
    ).pipe(timeout(this.TIMEOUT_MS));
  }
}
