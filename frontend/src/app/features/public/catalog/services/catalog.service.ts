import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../../core/http/api.service';
import { PaginatedResponse } from '../../../../shared/models/paginated-response.model';
import {
  ProfessionalSummary,
  ProfessionalDetail,
} from '../../../../shared/models/professional.model';
import { Category } from '../../../../shared/models/category.model';

export interface CatalogSearchParams {
  categoryId?: number;
  search?: string;
  page?: number;
  size?: number;
}

@Injectable({ providedIn: 'root' })
export class CatalogService {
  private readonly api = inject(ApiService);

  getProfessionals(
    params: CatalogSearchParams = {}
  ): Observable<PaginatedResponse<ProfessionalSummary>> {
    const queryParts: string[] = [];
    if (params.categoryId)
      queryParts.push(`categoryId=${params.categoryId}`);
    if (params.search?.trim())
      queryParts.push(`search=${encodeURIComponent(params.search.trim())}`);
    queryParts.push(`page=${params.page ?? 0}`);
    queryParts.push(`size=${params.size ?? 12}`);
    const query = queryParts.join('&');
    return this.api.get<PaginatedResponse<ProfessionalSummary>>(
      `/professionals?${query}`
    );
  }

  getProfessionalById(id: number): Observable<ProfessionalDetail> {
    return this.api.get<ProfessionalDetail>(`/professionals/${id}`);
  }

  getCategories(): Observable<Category[]> {
    return this.api.get<Category[]>('/categories');
  }
}
