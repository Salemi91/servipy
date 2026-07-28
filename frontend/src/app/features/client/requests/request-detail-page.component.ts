import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { ClientRequestService } from '../services/client-request.service';
import { ServiceRequestDetail } from '../models/service-request.model';
import { RequestStatusBadgeComponent } from './request-status-badge.component';

type PageState = 'loading' | 'loaded' | 'error' | 'not-found';

@Component({
  selector: 'app-request-detail-page',
  standalone: true,
  imports: [CommonModule, DatePipe, RouterLink, RequestStatusBadgeComponent],
  template: `
    <div>
      <!-- Back link -->
      <a
        routerLink="/client/requests"
        class="inline-flex items-center gap-1 text-sm text-blue-600 hover:underline mb-6"
      >
        ← Volver a Mis Solicitudes
      </a>

      <!-- Loading -->
      @if (state() === 'loading') {
        <div class="flex justify-center items-center py-16">
          <svg class="animate-spin h-8 w-8 text-blue-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"></path>
          </svg>
          <span class="ml-3 text-gray-600">Cargando detalle...</span>
        </div>
      }

      <!-- Error -->
      @if (state() === 'error') {
        <div class="flex flex-col items-center py-16">
          <p class="text-red-600 mb-4">No se pudo cargar el detalle de la solicitud.</p>
          <button
            (click)="loadDetail()"
            class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
          >
            Reintentar
          </button>
        </div>
      }

      <!-- Not found -->
      @if (state() === 'not-found') {
        <div class="flex flex-col items-center py-16">
          <p class="text-gray-500">Solicitud no encontrada.</p>
        </div>
      }

      <!-- Loaded -->
      @if (state() === 'loaded' && detail()) {
        <div class="bg-white rounded-lg shadow p-6">
          <div class="flex items-start justify-between mb-4">
            <h1 class="text-xl font-semibold text-gray-800">{{ detail()!.subject }}</h1>
            <app-request-status-badge [status]="detail()!.status" />
          </div>

          <div class="space-y-4">
            <div>
              <p class="text-sm font-medium text-gray-500">Profesional</p>
              <p class="text-gray-800">{{ detail()!.professionalName }}</p>
            </div>

            <div>
              <p class="text-sm font-medium text-gray-500">Descripción</p>
              <p class="text-gray-800 whitespace-pre-line">{{ detail()!.description }}</p>
            </div>

            @if (detail()!.desiredDate) {
              <div>
                <p class="text-sm font-medium text-gray-500">Fecha deseada</p>
                <p class="text-gray-800">{{ detail()!.desiredDate }}</p>
              </div>
            }

            <div class="flex gap-8 pt-4 border-t border-gray-100">
              <div>
                <p class="text-xs text-gray-400">Creado</p>
                <p class="text-sm text-gray-600">{{ detail()!.createdAt | date:'dd/MM/yyyy HH:mm' }}</p>
              </div>
              <div>
                <p class="text-xs text-gray-400">Última actualización</p>
                <p class="text-sm text-gray-600">{{ detail()!.updatedAt | date:'dd/MM/yyyy HH:mm' }}</p>
              </div>
            </div>
          </div>
        </div>
      }
    </div>
  `,
})
export class RequestDetailPageComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly clientRequestService = inject(ClientRequestService);

  state = signal<PageState>('loading');
  detail = signal<ServiceRequestDetail | null>(null);

  private requestId = 0;

  ngOnInit(): void {
    this.requestId = Number(this.route.snapshot.paramMap.get('id'));
    if (!this.requestId) {
      this.state.set('not-found');
      return;
    }
    this.loadDetail();
  }

  loadDetail(): void {
    this.state.set('loading');
    this.clientRequestService.getRequestDetail(this.requestId).subscribe({
      next: (res) => {
        this.detail.set(res);
        this.state.set('loaded');
      },
      error: (err) => {
        if (err.status === 404) {
          this.state.set('not-found');
        } else {
          this.state.set('error');
        }
      },
    });
  }
}
