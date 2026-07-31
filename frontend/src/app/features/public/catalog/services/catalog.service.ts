import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../../core/http/api.service';
import { PaginatedResponse } from '../../../../shared/models/paginated-response.model';
import {
  ProfessionalSummary,
  ProfessionalDetail,
} from '../../../../shared/models/professional.model';
import { Category } from '../../../../shared/models/category.model';
import { ReferenceDataService } from '../../../../core/services/reference-data.service';

export interface CatalogSearchParams {
  categoryId?: number;
  cityId?: number;
  search?: string;
  page?: number;
  size?: number;
}

@Injectable({ providedIn: 'root' })
export class CatalogService {
  private readonly api = inject(ApiService);
  private readonly referenceData = inject(ReferenceDataService);

  getProfessionals(
    params: CatalogSearchParams = {}
  ): Observable<PaginatedResponse<ProfessionalSummary>> {
    const queryParts: string[] = [];
    if (params.categoryId)
      queryParts.push(`categoryId=${params.categoryId}`);
    if (params.cityId)
      queryParts.push(`cityId=${params.cityId}`);
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
    return this.referenceData.getCategories();
  }

  /**
   * Cantidad de profesionales visibles por categoría (id de categoría -> conteo).
   * Las categorías sin profesionales visibles no aparecen en el mapa.
   */
  getCategoryCounts(): Observable<Record<number, number>> {
    return this.api.get<Record<number, number>>('/professionals/category-counts');
  }
}
