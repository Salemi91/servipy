import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminCategoryService, CreateCategoryRequest } from '../services/admin-category.service';
import { Category } from '../../../shared/models/category.model';

@Component({
  selector: 'app-category-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="p-6">
      <div class="flex justify-between items-center mb-6">
        <h2 class="text-2xl font-semibold text-gray-800">Categorías</h2>
        <button
          (click)="showCreateModal = true"
          class="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700">
          + Nueva Categoría
        </button>
      </div>

      @if (loading) {
        <p class="text-gray-500">Cargando categorías...</p>
      }

      @if (!loading && categories.length === 0) {
        <p class="text-gray-500">No hay categorías registradas.</p>
      }

      @if (!loading && categories.length > 0) {
        <div class="overflow-x-auto rounded-lg border border-gray-200">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Nombre</th>
                <th class="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Descripción</th>
                <th class="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Icono</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-200 bg-white">
              @for (cat of categories; track cat.id) {
                <tr>
                  <td class="whitespace-nowrap px-6 py-4 text-sm font-medium text-gray-900">{{ cat.name }}</td>
                  <td class="px-6 py-4 text-sm text-gray-600">{{ cat.description || '—' }}</td>
                  <td class="whitespace-nowrap px-6 py-4 text-sm text-gray-500">{{ cat.icon || '—' }}</td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }

      <!-- Create Category Modal -->
      @if (showCreateModal) {
        <div class="fixed inset-0 z-50 flex items-center justify-center bg-black/50" (click)="closeOnBackdrop($event)">
          <div class="bg-white rounded-lg shadow-xl w-full max-w-md mx-4 p-6">
            <div class="flex justify-between items-center mb-4">
              <h3 class="text-lg font-semibold text-gray-900">Nueva Categoría</h3>
              <button (click)="showCreateModal = false" class="text-gray-400 hover:text-gray-600">&#x2715;</button>
            </div>

            <form (ngSubmit)="onCreateCategory()" class="space-y-4">
              <div>
                <label for="catName" class="block text-sm font-medium text-gray-700">Nombre</label>
                <input id="catName" type="text" [(ngModel)]="newCategory.name" name="name" required
                  class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500"
                  placeholder="Ej: Plomería" />
              </div>
              <div>
                <label for="catIcon" class="block text-sm font-medium text-gray-700">Icono</label>
                <input id="catIcon" type="text" [(ngModel)]="newCategory.icon" name="icon"
                  class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500"
                  placeholder="Ej: wrench" />
              </div>
              <div>
                <label for="catDesc" class="block text-sm font-medium text-gray-700">Descripción</label>
                <textarea id="catDesc" [(ngModel)]="newCategory.description" name="description" rows="3"
                  class="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500"
                  placeholder="Descripción de la categoría"></textarea>
              </div>

              @if (createError) {
                <p class="text-sm text-red-600">{{ createError }}</p>
              }

              <div class="flex justify-end gap-3 pt-2">
                <button type="button" (click)="showCreateModal = false"
                  class="rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50">
                  Cancelar
                </button>
                <button type="submit" [disabled]="!newCategory.name || creating"
                  class="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50">
                  {{ creating ? 'Creando...' : 'Crear' }}
                </button>
              </div>
            </form>
          </div>
        </div>
      }
    </div>
  `,
})
export class CategoryListComponent implements OnInit {
  private readonly service = inject(AdminCategoryService);

  categories: Category[] = [];
  loading = true;
  showCreateModal = false;
  creating = false;
  createError = '';

  newCategory: CreateCategoryRequest = { name: '', icon: '', description: '' };

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.loading = true;
    this.service.getCategories().subscribe({
      next: (data) => {
        this.categories = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  onCreateCategory(): void {
    this.creating = true;
    this.createError = '';
    this.service.create(this.newCategory).subscribe({
      next: () => {
        this.creating = false;
        this.showCreateModal = false;
        this.newCategory = { name: '', icon: '', description: '' };
        this.loadCategories();
      },
      error: (err) => {
        this.creating = false;
        this.createError = err?.error?.message || 'Error al crear la categoría';
      },
    });
  }

  closeOnBackdrop(event: MouseEvent): void {
    if (event.target === event.currentTarget) {
      this.showCreateModal = false;
    }
  }
}
