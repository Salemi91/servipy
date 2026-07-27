import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/http/api.service';
import { Category } from '../../../shared/models/category.model';

export interface CreateCategoryRequest {
  name: string;
  icon: string;
  description: string;
}

@Injectable({ providedIn: 'root' })
export class AdminCategoryService {
  private readonly api = inject(ApiService);

  getCategories(): Observable<Category[]> {
    return this.api.get<Category[]>('/categories');
  }

  create(request: CreateCategoryRequest): Observable<Category> {
    return this.api.post<Category>('/admin/categories', request);
  }
}
