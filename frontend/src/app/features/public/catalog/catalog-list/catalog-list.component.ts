import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';

import { CatalogService } from '../services/catalog.service';
import { ProfessionalSummary } from '../../../../shared/models/professional.model';
import { Category } from '../../../../shared/models/category.model';
import { PaginatedResponse } from '../../../../shared/models/paginated-response.model';

import { ProfessionalCardComponent } from '../components/professional-card/professional-card.component';
import { CategoryFilterComponent } from '../components/category-filter/category-filter.component';
import { SearchBarComponent } from '../components/search-bar/search-bar.component';
import { EmptyStateComponent } from '../components/empty-state/empty-state.component';
import { LoadingStateComponent } from '../components/loading-state/loading-state.component';
import { ErrorStateComponent } from '../components/error-state/error-state.component';

type CatalogState = 'loading' | 'loaded' | 'error';

@Component({
  selector: 'app-catalog-list',
  standalone: true,
  imports: [
    CommonModule,
    ProfessionalCardComponent,
    CategoryFilterComponent,
    SearchBarComponent,
    EmptyStateComponent,
    LoadingStateComponent,
    ErrorStateComponent,
  ],
  templateUrl: './catalog-list.component.html',
})
export class CatalogListComponent implements OnInit {
  private catalogService: CatalogService;
  private readonly route = inject(ActivatedRoute);

  state = signal<CatalogState>('loading');
  professionals = signal<ProfessionalSummary[]>([]);
  categories = signal<Category[]>([]);
  totalElements = signal(0);
  currentPage = signal(0);
  totalPages = signal(0);

  selectedCategoryId = signal<number | null>(null);
  selectedCityId = signal<number | null>(null);
  searchTerm = signal('');

  hasFilters = computed(
    () =>
      this.selectedCategoryId() !== null ||
      this.selectedCityId() !== null ||
      this.searchTerm().trim().length > 0
  );

  constructor(catalogService: CatalogService) {
    this.catalogService = catalogService;
  }

  ngOnInit(): void {
    // Permite llegar con filtros preseleccionados desde el buscador del home
    // (?categoryId=&cityId=&search=).
    const params = this.route.snapshot.queryParamMap;
    const categoryId = params.get('categoryId');
    const cityId = params.get('cityId');
    const search = params.get('search');

    if (categoryId) this.selectedCategoryId.set(Number(categoryId));
    if (cityId) this.selectedCityId.set(Number(cityId));
    if (search) this.searchTerm.set(search);

    this.loadCategories();
    this.loadProfessionals();
  }

  onCategorySelected(categoryId: number | null): void {
    this.selectedCategoryId.set(categoryId);
    this.currentPage.set(0);
    this.loadProfessionals();
  }

  onSearchChanged(term: string): void {
    this.searchTerm.set(term);
    this.currentPage.set(0);
    this.loadProfessionals();
  }

  onClearFilters(): void {
    this.selectedCategoryId.set(null);
    this.selectedCityId.set(null);
    this.searchTerm.set('');
    this.currentPage.set(0);
    this.loadProfessionals();
  }

  onRetry(): void {
    this.loadProfessionals();
  }

  onLoadMore(): void {
    this.currentPage.update((p) => p + 1);
    this.loadProfessionalsAppend();
  }

  private loadCategories(): void {
    this.catalogService.getCategories().subscribe({
      next: (cats) => this.categories.set(cats),
    });
  }

  private loadProfessionals(): void {
    this.state.set('loading');
    this.catalogService
      .getProfessionals({
        categoryId: this.selectedCategoryId() ?? undefined,
        cityId: this.selectedCityId() ?? undefined,
        search: this.searchTerm() || undefined,
        page: this.currentPage(),
        size: 12,
      })
      .subscribe({
        next: (res: PaginatedResponse<ProfessionalSummary>) => {
          this.professionals.set(res.content);
          this.totalElements.set(res.totalElements);
          this.totalPages.set(res.totalPages);
          this.state.set('loaded');
        },
        error: () => {
          this.state.set('error');
        },
      });
  }

  private loadProfessionalsAppend(): void {
    this.catalogService
      .getProfessionals({
        categoryId: this.selectedCategoryId() ?? undefined,
        cityId: this.selectedCityId() ?? undefined,
        search: this.searchTerm() || undefined,
        page: this.currentPage(),
        size: 12,
      })
      .subscribe({
        next: (res: PaginatedResponse<ProfessionalSummary>) => {
          this.professionals.update((prev) => [...prev, ...res.content]);
          this.totalElements.set(res.totalElements);
          this.totalPages.set(res.totalPages);
        },
        error: () => {
          this.currentPage.update((p) => p - 1);
        },
      });
  }
}
