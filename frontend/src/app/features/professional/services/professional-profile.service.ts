import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/http/api.service';
import {
  OfferedServiceForm,
  OfferedServiceItem,
  ProfessionalProfileForm,
} from '../../../shared/models/professional.model';

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

  /**
   * GET /api/v1/professional/profile/services
   */
  getMyServices(): Observable<OfferedServiceItem[]> {
    return this.api.get<OfferedServiceItem[]>('/professional/profile/services');
  }

  /**
   * POST /api/v1/professional/profile/services
   * Publishes one service of the professional's price list.
   */
  createService(data: OfferedServiceForm): Observable<OfferedServiceItem> {
    const payload = {
      categoryId: Number(data.categoryId),
      name: data.name,
      description: data.description || null,
      price: data.price,
      currency: 'PYG',
    };
    return this.api.post<OfferedServiceItem>('/professional/profile/services', payload);
  }

  /**
   * DELETE /api/v1/professional/profile/services/{id}
   */
  deleteService(serviceId: number): Observable<void> {
    return this.api.delete<void>(`/professional/profile/services/${serviceId}`);
  }
}
