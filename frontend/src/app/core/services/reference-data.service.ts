import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../http/api.service';
import { Category } from '../../shared/models/category.model';
import { City } from '../../shared/models/city.model';

/**
 * Catálogos de referencia compartidos por varias features
 * (filtros del catálogo público y onboarding del profesional).
 */
@Injectable({ providedIn: 'root' })
export class ReferenceDataService {
  private readonly api = inject(ApiService);

  getCategories(): Observable<Category[]> {
    return this.api.get<Category[]>('/categories');
  }

  getCities(): Observable<City[]> {
    return this.api.get<City[]>('/cities');
  }
}
