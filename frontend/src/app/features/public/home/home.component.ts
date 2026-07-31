import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { ReferenceDataService } from '../../../core/services/reference-data.service';
import { CatalogService } from '../catalog/services/catalog.service';
import { Category } from '../../../shared/models/category.model';
import { City } from '../../../shared/models/city.model';

interface CategoryCard extends Category {
  professionalCount: number;
}

/**
 * Íconos SVG inline por nombre de categoría (en minúsculas), para no depender
 * de una librería de iconos externa. "Otros" se usa como fallback.
 */
const CATEGORY_ICON_PATHS: Record<string, string> = {
  plomería:
    'M12 2a5 5 0 00-5 5v2H5a1 1 0 00-1 1v3a5 5 0 005 5v2a1 1 0 001 1h4a1 1 0 001-1v-2a5 5 0 005-5v-3a1 1 0 00-1-1h-2V7a5 5 0 00-5-5z',
  electricidad: 'M13 2L3 14h6l-1 8 10-12h-6l1-8z',
  limpieza:
    'M5 3l1.5 1.5M19 3l-1.5 1.5M12 2v3m-7 8a7 7 0 1014 0 7 7 0 00-14 0zm7 0v6',
  pintura:
    'M7 21a2 2 0 01-2-2v-4l9-9 4 4-9 9H7zm9-13l2-2a2 2 0 013 3l-2 2',
  jardinería:
    'M12 22c4-2 6-6 6-10a6 6 0 00-12 0c0 4 2 8 6 10zM12 12v10',
  carpintería:
    'M4 4l7 7m0 0l9 9m-9-9l5-5m-5 5L4 20',
  cerrajería:
    'M12 15v4m-4-9a4 4 0 118 0v2H8v-2zm-2 2h12v8H6v-8z',
  mudanzas:
    'M3 7h11v9H3V7zm11 3h4l3 3v3h-7v-6zM6 19a1.5 1.5 0 100-3 1.5 1.5 0 000 3zm11 0a1.5 1.5 0 100-3 1.5 1.5 0 000 3z',
  'aire acondicionado':
    'M4 10h16M4 14h16M8 10v8m4-8v8m4-8v8M2 10l2-4h16l2 4',
  albañilería:
    'M4 6h6v4H4V6zm10 0h6v4h-6V6zM4 14h6v4H4v-4zm10 0h6v4h-6v-4z',
};

const DEFAULT_ICON_PATH =
  'M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './home.component.html',
})
export class HomeComponent implements OnInit {
  private readonly referenceData = inject(ReferenceDataService);
  private readonly catalogService = inject(CatalogService);
  private readonly router = inject(Router);

  readonly categories = signal<CategoryCard[]>([]);
  readonly cities = signal<City[]>([]);
  readonly categoriesState = signal<'loading' | 'loaded' | 'error'>('loading');

  // Buscador del hero
  searchCategoryId: number | null = null;
  searchCityId: number | null = null;
  searchTerm = '';
  readonly searching = signal(false);

  readonly steps = [
    {
      number: 1,
      title: 'Encontrá',
      description: 'Buscá por categoría, ciudad o palabra clave entre profesionales verificados.',
    },
    {
      number: 2,
      title: 'Solicitá',
      description: 'Revisá el perfil y el tarifario, y enviá tu solicitud con los detalles del trabajo.',
    },
    {
      number: 3,
      title: 'Conectá',
      description: 'Cuando el profesional acepta, se habilita el contacto directo por WhatsApp.',
    },
  ];

  ngOnInit(): void {
    this.loadCategories();
    this.referenceData.getCities().subscribe({
      next: (cities) => this.cities.set(cities),
    });
  }

  iconPathFor(categoryName: string): string {
    return CATEGORY_ICON_PATHS[categoryName.toLowerCase()] ?? DEFAULT_ICON_PATH;
  }

  onSearch(): void {
    this.searching.set(true);
    const queryParams: Record<string, string> = {};
    if (this.searchCategoryId) queryParams['categoryId'] = String(this.searchCategoryId);
    if (this.searchCityId) queryParams['cityId'] = String(this.searchCityId);
    if (this.searchTerm.trim()) queryParams['search'] = this.searchTerm.trim();

    this.router.navigate(['/profesionales'], { queryParams });
  }

  onCategoryCardSelected(categoryId: number): void {
    this.router.navigate(['/profesionales'], { queryParams: { categoryId } });
  }

  private loadCategories(): void {
    this.categoriesState.set('loading');
    this.referenceData.getCategories().subscribe({
      next: (categories) => {
        this.catalogService.getCategoryCounts().subscribe({
          next: (counts) => {
            this.applyCategoryCounts(categories, counts);
          },
          error: () => {
            // Si el conteo falla, igual mostramos las categorías sin número.
            this.applyCategoryCounts(categories, {});
          },
        });
      },
      error: () => this.categoriesState.set('error'),
    });
  }

  private applyCategoryCounts(categories: Category[], counts: Record<number, number>): void {
    const cards: CategoryCard[] = categories.map((c) => ({
      ...c,
      professionalCount: counts[c.id] ?? 0,
    }));
    this.categories.set(cards);
    this.categoriesState.set('loaded');
  }
}
