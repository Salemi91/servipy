import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/http/api.service';
import { ProfessionalAdmin } from '../../../shared/models/professional-admin.model';

@Injectable({ providedIn: 'root' })
export class AdminProfessionalService {
  private readonly api = inject(ApiService);

  getPending(): Observable<ProfessionalAdmin[]> {
    return this.api.get<ProfessionalAdmin[]>('/admin/professionals/pending');
  }

  approve(id: number): Observable<ProfessionalAdmin> {
    return this.api.patch<ProfessionalAdmin>(`/admin/professionals/${id}/approve`, {});
  }

  reject(id: number): Observable<ProfessionalAdmin> {
    return this.api.patch<ProfessionalAdmin>(`/admin/professionals/${id}/reject`, {});
  }
}
