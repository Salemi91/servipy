import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { ServiceRequestService } from '../services/service-request.service';
import {
  ServiceRequestDetail,
  ChangeStatusPayload,
} from '../../../../shared/models/service-request.model';
import { ErrorResponse } from '../../../../shared/models/api-response.model';
import { ProfessionalProfileApiService } from '../../services/professional-profile.service';

type DetailState = 'loading' | 'loaded' | 'not-found' | 'error';

@Component({
  selector: 'app-request-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './request-detail.component.html',
})
export class RequestDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly serviceRequestService = inject(ServiceRequestService);
  private readonly profileApi = inject(ProfessionalProfileApiService);

  state = signal<DetailState>('loading');
  request = signal<ServiceRequestDetail | null>(null);
  isUpdating = signal(false);
  statusMessage = signal<{ type: 'success' | 'error'; text: string } | null>(null);

  professionalId = 0;
  requestId = 0;

  ngOnInit(): void {
    this.requestId = Number(this.route.snapshot.paramMap.get('requestId') ?? '0');
    this.profileApi.getMyProfile().subscribe({
      next: (profile) => {
        this.professionalId = profile.id;
        this.loadDetail();
      },
      error: () => {
        this.state.set('error');
      },
    });
  }

  onAccept(): void {
    this.changeStatus('ACCEPTED');
  }

  onReject(): void {
    this.changeStatus('REJECTED');
  }

  onRetry(): void {
    this.loadDetail();
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'PENDING':
        return 'Pendiente';
      case 'ACCEPTED':
        return 'Aceptada';
      case 'REJECTED':
        return 'Rechazada';
      default:
        return status;
    }
  }

  getStatusClasses(status: string): string {
    switch (status) {
      case 'PENDING':
        return 'bg-amber-100 text-amber-800';
      case 'ACCEPTED':
        return 'bg-green-100 text-green-800';
      case 'REJECTED':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  private loadDetail(): void {
    this.state.set('loading');
    this.statusMessage.set(null);
    this.serviceRequestService
      .getDetail(this.professionalId, this.requestId)
      .subscribe({
        next: (detail) => {
          this.request.set(detail);
          this.state.set('loaded');
        },
        error: (err: HttpErrorResponse) => {
          if (err.status === 404) {
            this.state.set('not-found');
          } else {
            this.state.set('error');
          }
        },
      });
  }

  private changeStatus(status: 'ACCEPTED' | 'REJECTED'): void {
    this.isUpdating.set(true);
    this.statusMessage.set(null);

    const payload: ChangeStatusPayload = { status };

    this.serviceRequestService
      .changeStatus(this.professionalId, this.requestId, payload)
      .subscribe({
        next: () => {
          this.statusMessage.set({
            type: 'success',
            text: status === 'ACCEPTED'
              ? 'Solicitud aceptada exitosamente.'
              : 'Solicitud rechazada exitosamente.',
          });
          this.isUpdating.set(false);
          this.loadDetail();
        },
        error: (err: HttpErrorResponse) => {
          this.isUpdating.set(false);
          if (err.status === 409) {
            const errorResponse = err.error as ErrorResponse;
            this.statusMessage.set({
              type: 'error',
              text: errorResponse?.message || 'No se puede cambiar el estado de esta solicitud.',
            });
          } else {
            this.statusMessage.set({
              type: 'error',
              text: 'Ocurrió un error inesperado. Intente nuevamente.',
            });
          }
        },
      });
  }
}
