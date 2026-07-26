import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { ServiceRequestService } from '../services/service-request.service';
import { ServiceRequestSummary } from '../../../../shared/models/service-request.model';

type ListState = 'loading' | 'loaded' | 'error';

@Component({
  selector: 'app-request-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './request-list.component.html',
})
export class RequestListComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly serviceRequestService = inject(ServiceRequestService);

  state = signal<ListState>('loading');
  requests = signal<ServiceRequestSummary[]>([]);
  selectedStatus = signal<string>('');
  professionalId = 0;

  ngOnInit(): void {
    this.professionalId = Number(
      this.route.parent?.snapshot.paramMap.get('professionalId') ?? '0'
    );
    this.loadRequests();
  }

  onFilterChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    this.selectedStatus.set(value);
    this.loadRequests();
  }

  onRetry(): void {
    this.loadRequests();
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

  private loadRequests(): void {
    this.state.set('loading');
    const status = this.selectedStatus() || undefined;
    this.serviceRequestService
      .getByProfessional(this.professionalId, status)
      .subscribe({
        next: (data) => {
          this.requests.set(data);
          this.state.set('loaded');
        },
        error: () => {
          this.state.set('error');
        },
      });
  }
}
