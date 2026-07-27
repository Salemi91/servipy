import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminProfessionalService } from '../services/admin-professional.service';
import { ProfessionalAdmin } from '../../../shared/models/professional-admin.model';

@Component({
  selector: 'app-professional-list',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="p-6">
      <h2 class="text-2xl font-semibold text-gray-800 mb-6">Profesionales Pendientes</h2>

      @if (loading) {
        <p class="text-gray-500">Cargando profesionales...</p>
      }

      @if (!loading && professionals.length === 0) {
        <div class="text-center py-12">
          <p class="text-gray-500 text-lg">No hay profesionales pendientes de aprobación.</p>
        </div>
      }

      @if (!loading && professionals.length > 0) {
        <div class="overflow-x-auto rounded-lg border border-gray-200">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Nombre</th>
                <th class="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Email</th>
                <th class="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Teléfono</th>
                <th class="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Descripción</th>
                <th class="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Fecha</th>
                <th class="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Acciones</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-200 bg-white">
              @for (prof of professionals; track prof.id) {
                <tr>
                  <td class="whitespace-nowrap px-6 py-4 text-sm font-medium text-gray-900">{{ prof.name }}</td>
                  <td class="px-6 py-4 text-sm text-gray-600">{{ prof.email }}</td>
                  <td class="whitespace-nowrap px-6 py-4 text-sm text-gray-500">{{ prof.phone || '—' }}</td>
                  <td class="px-6 py-4 text-sm text-gray-500 max-w-xs truncate">{{ prof.description || '—' }}</td>
                  <td class="whitespace-nowrap px-6 py-4 text-sm text-gray-500">{{ prof.createdAt | date:'short' }}</td>
                  <td class="whitespace-nowrap px-6 py-4 text-sm space-x-2">
                    <button
                      (click)="openConfirmDialog(prof, 'approve')"
                      class="rounded bg-green-600 px-3 py-1 text-xs font-medium text-white hover:bg-green-700">
                      Aprobar
                    </button>
                    <button
                      (click)="openConfirmDialog(prof, 'reject')"
                      class="rounded bg-red-600 px-3 py-1 text-xs font-medium text-white hover:bg-red-700">
                      Rechazar
                    </button>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }

      <!-- Confirmation Dialog -->
      @if (showConfirmDialog) {
        <div class="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
          <div class="bg-white rounded-lg shadow-xl w-full max-w-sm mx-4 p-6">
            <h3 class="text-lg font-semibold text-gray-900 mb-2">{{ confirmTitle }}</h3>
            <p class="text-sm text-gray-600 mb-6">{{ confirmMessage }}</p>
            <div class="flex justify-end gap-3">
              <button
                (click)="cancelAction()"
                class="rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50">
                Cancelar
              </button>
              <button
                (click)="executeAction()"
                [class]="pendingAction === 'approve'
                  ? 'rounded-md bg-green-600 px-4 py-2 text-sm font-medium text-white hover:bg-green-700'
                  : 'rounded-md bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700'">
                Confirmar
              </button>
            </div>
          </div>
        </div>
      }
    </div>
  `,
})
export class ProfessionalListComponent implements OnInit {
  private readonly service = inject(AdminProfessionalService);

  professionals: ProfessionalAdmin[] = [];
  loading = true;

  showConfirmDialog = false;
  confirmTitle = '';
  confirmMessage = '';
  pendingAction: 'approve' | 'reject' = 'approve';
  selectedProfessional: ProfessionalAdmin | null = null;

  ngOnInit(): void {
    this.loadProfessionals();
  }

  loadProfessionals(): void {
    this.loading = true;
    this.service.getPending().subscribe({
      next: (data) => {
        this.professionals = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  openConfirmDialog(prof: ProfessionalAdmin, action: 'approve' | 'reject'): void {
    this.selectedProfessional = prof;
    this.pendingAction = action;
    this.confirmTitle = action === 'approve' ? 'Aprobar Profesional' : 'Rechazar Profesional';
    this.confirmMessage = action === 'approve'
      ? `¿Desea aprobar a ${prof.name}? Podrá ofrecer servicios en la plataforma.`
      : `¿Desea rechazar a ${prof.name}? No podrá ofrecer servicios.`;
    this.showConfirmDialog = true;
  }

  cancelAction(): void {
    this.showConfirmDialog = false;
    this.selectedProfessional = null;
  }

  executeAction(): void {
    if (!this.selectedProfessional) return;
    const id = this.selectedProfessional.id;

    const action$ = this.pendingAction === 'approve'
      ? this.service.approve(id)
      : this.service.reject(id);

    action$.subscribe({
      next: () => {
        this.showConfirmDialog = false;
        this.selectedProfessional = null;
        this.loadProfessionals();
      },
      error: () => {
        this.showConfirmDialog = false;
      },
    });
  }
}
