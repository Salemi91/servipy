import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../../core/http/api.service';
import {
  CreateServiceRequestPayload,
  CreateServiceRequestResponse,
  ServiceRequestSummary,
  ServiceRequestDetail,
  ChangeStatusPayload,
} from '../../../../shared/models/service-request.model';

@Injectable({ providedIn: 'root' })
export class ServiceRequestService {
  private readonly api = inject(ApiService);

  create(
    professionalId: number,
    payload: CreateServiceRequestPayload
  ): Observable<CreateServiceRequestResponse> {
    return this.api.post<CreateServiceRequestResponse>(
      `/professionals/${professionalId}/service-requests`,
      payload
    );
  }

  getByProfessional(
    professionalId: number,
    status?: string
  ): Observable<ServiceRequestSummary[]> {
    const query = status ? `?status=${status}` : '';
    return this.api.get<ServiceRequestSummary[]>(
      `/professionals/${professionalId}/service-requests${query}`
    );
  }

  getDetail(
    professionalId: number,
    requestId: number
  ): Observable<ServiceRequestDetail> {
    return this.api.get<ServiceRequestDetail>(
      `/professionals/${professionalId}/service-requests/${requestId}`
    );
  }

  changeStatus(
    professionalId: number,
    requestId: number,
    payload: ChangeStatusPayload
  ): Observable<void> {
    return this.api.patch<void>(
      `/professionals/${professionalId}/service-requests/${requestId}/status`,
      payload
    );
  }
}
