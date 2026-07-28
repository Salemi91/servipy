import { Component, OnInit, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { timeout, TimeoutError } from 'rxjs';

import { ClientRequestService } from '../services/client-request.service';
import { ServiceRequest, PaginatedResponse } from '../models/service-request.model';
import { RequestStatusBadgeComponent } from './request-status-badge.component';

type PageState = 'loading' | 'loaded' | 'error';

@Component({
  selector: 'app-request-history-page',
  standalone: true,
  imports: [CommonModule, DatePipe, RequestStatusBadgeComponent, RouterLink],
  template: `
    <div>
      <div class="flex flex-wrap items-center justify-between gap-4 mb-6">
        <h1 class="text-2xl font-semibold text-gray-800">Mis Solicitudes</h1>
        <a
          routerLink="/profesionales"
          class="inline-flex items-center gap-2 rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 transition-colors"
        >
          + Nueva Solicitud
        </a>
      </div>

      <!-- Filter row -->
      <div class="flex flex-wrap items-center justify-between gap-4 mb-6">
        <select
          [value]="selectedStatus()"
          (change)="onStatusFilterChange($event)"
          [disabled]="state() === 'loading'"
          class="border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <option value="">Todas</option>
          <option value="PENDING">Pendiente</option>
          <option value="ACCEPTED">Aceptada</option>
          <option value="REJECTED">Rechazada</option>
        </select>

        @if (state() === 'loaded' && requests().length > 0) {
          <span class="text-sm text-gray-500">
            Mostrando {{ requests().length }} de {{ totalElements() }} solicitudes
          </span>
        }
      </div>

      <!-- Loading -->
      @if (state() === 'loading') {
        <div class="flex justify-center items-center py-16">
          <svg class="animate-spin h-8 w-8 text-blue-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"></path>
          </svg>
          <span class="ml-3 text-gray-600">Cargando solicitudes...</span>
        </div>
      }

      <!-- Error -->
      @if (state() === 'error') {
        <div class="flex flex-col items-center py-16">
          <p class="text-red-600 mb-4">{{ errorMessage() }}</p>
          <button
            (click)="loadRequests()"
            class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
          >
            Reintentar
          </button>
        </div>
      }

      <!-- Empty -->
      @if (state() === 'loaded' && requests().length === 0) {
        <div class="flex flex-col items-center py-16">
          <p class="text-gray-500 mb-4">No ha realizado solicitudes aún.</p>
          <a
            routerLink="/profesionales"
            class="inline-flex items-center gap-2 rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 transition-colors"
          >
            Buscar un profesional
          </a>
        </div>
      }

      <!-- List -->
      @if (state() === 'loaded' && requests().length > 0) {
        <div class="space-y-4">
          @for (request of requests(); track request.id) {
            <a
              [routerLink]="['/client/requests', request.id]"
              class="block bg-white rounded-lg shadow p-4 hover:shadow-md hover:border-blue-200 border border-transparent transition-all cursor-pointer"
            >
              <div class="flex justify-between items-start">
                <div>
                  <p class="font-medium text-gray-800">{{ request.serviceName }}</p>
                  <p class="text-sm text-gray-500">{{ request.professionalName }}</p>
                  <p class="text-xs text-gray-400 mt-1">
                    Creado: {{ request.createdAt | date:'dd/MM/yyyy HH:mm' }}
                  </p>
                </div>
                <div class="flex flex-col items-end gap-1">
                  <app-request-status-badge [status]="request.status" />
                  <p class="text-xs text-gray-400">
                    Actualizado: {{ request.updatedAt | date:'dd/MM/yyyy HH:mm' }}
                  </p>
                </div>
              </div>
            </a>
          }
        </div>

        <!-- Pagination controls -->
        <div class="flex items-center justify-between mt-6">
          <button
            (click)="onPreviousPage()"
            [disabled]="currentPage() === 0"
            class="px-4 py-2 border border-gray-300 rounded-md text-sm text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Anterior
          </button>
          <span class="text-sm text-gray-500">
            Página {{ currentPage() + 1 }} de {{ totalPages() }}
          </span>
          <button
            (click)="onNextPage()"
            [disabled]="currentPage() >= totalPages() - 1"
            class="px-4 py-2 border border-gray-300 rounded-md text-sm text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Siguiente
          </button>
        </div>
      }
    </div>
  `,
})
export class RequestHistoryPageComponent implements OnInit {
  private readonly clientRequestService: ClientRequestService;

  state = signal<PageState>('loading');
  requests = signal<ServiceRequest[]>([]);
  selectedStatus = signal<string>('');
  errorMessage = signal<string>('No se pudieron cargar las solicitudes. Intente nuevamente.');

  // Pagination metadata
  totalElements = signal(0);
  totalPages = signal(0);
  currentPage = signal(0);
  pageSize = signal(20);

  constructor(clientRequestService: ClientRequestService) {
    this.clientRequestService = clientRequestService;
  }

  ngOnInit(): void {
    this.loadRequests();
  }

  loadRequests(): void {
    this.state.set('loading');
    const status = this.selectedStatus() || undefined;

    this.clientRequestService
      .getRequests(status, this.currentPage(), this.pageSize())
      .pipe(timeout(30000))
      .subscribe({
        next: (res: PaginatedResponse<ServiceRequest>) => {
          this.requests.set(res.content);
          this.totalElements.set(res.totalElements);
          this.totalPages.set(res.totalPages);
          this.currentPage.set(res.currentPage);
          this.state.set('loaded');
        },
        error: (err) => {
          this.errorMessage.set(
            err instanceof TimeoutError
              ? 'La operación no pudo completarse por tiempo de espera agotado.'
              : 'No se pudieron cargar las solicitudes. Intente nuevamente.'
          );
          this.state.set('error');
        },
      });
  }

  onStatusFilterChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    this.selectedStatus.set(value);
    this.currentPage.set(0);
    this.loadRequests();
  }

  onPreviousPage(): void {
    if (this.currentPage() > 0) {
      this.currentPage.update((p) => p - 1);
      this.loadRequests();
    }
  }

  onNextPage(): void {
    if (this.currentPage() < this.totalPages() - 1) {
      this.currentPage.update((p) => p + 1);
      this.loadRequests();
    }
  }
}
