import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/http/api.service';
import { ProfessionalProfileForm } from '../../../shared/models/professional.model';

export interface ProfileMeResponse {
  id: number;
  phone: string;
  whatsapp: string;
  description: string;
  cityId: number;
  cityName: string;
  availability: string;
  approvalStatus: string;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class ProfessionalProfileApiService {
  private readonly api = inject(ApiService);

  /**
   * GET /api/v1/professional/profile/me
   * Returns the logged-in professional's profile or throws 404.
   */
  getMyProfile(): Observable<ProfileMeResponse> {
    return this.api.get<ProfileMeResponse>('/professional/profile/me');
  }

  /**
   * POST /api/v1/professional/profile
   * Creates the professional profile (onboarding).
   */
  createProfile(data: ProfessionalProfileForm): Observable<ProfileMeResponse> {
    const payload = {
      phone: data.phone,
      whatsapp: data.whatsapp || data.phone,
      description: data.description,
      cityId: Number(data.city),
      availability: data.availability || 'PRESENCIAL',
    };
    return this.api.post<ProfileMeResponse>('/professional/profile', payload);
  }
}
