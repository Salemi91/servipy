import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { timeout } from 'rxjs';
import { API_BASE_URL } from '../../../core/config/api.config';
import { ServiceRequest, ServiceRequestDetail, PaginatedResponse } from '../models/service-request.model';

@Injectable({ providedIn: 'root' })
export class ClientRequestService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);

  private readonly TIMEOUT_MS = 15000;

  getRequests(status?: string, page = 0, size = 20): Observable<PaginatedResponse<ServiceRequest>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (status) {
      params = params.set('status', status);
    }

    return this.http.get<PaginatedResponse<ServiceRequest>>(
      `${this.baseUrl}/client/requests`,
      { params }
    ).pipe(timeout(this.TIMEOUT_MS));
  }

  getRequestDetail(id: number): Observable<ServiceRequestDetail> {
    return this.http.get<ServiceRequestDetail>(
      `${this.baseUrl}/client/requests/${id}`
    ).pipe(timeout(this.TIMEOUT_MS));
  }
}
