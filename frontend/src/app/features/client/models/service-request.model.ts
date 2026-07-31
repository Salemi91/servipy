export type RequestStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED';

export interface ServiceRequest {
  id: number;
  serviceName: string;
  professionalName: string;
  status: RequestStatus;
  createdAt: string;
  updatedAt: string;
}

export interface ServiceRequestDetail {
  id: number;
  professionalName: string;
  subject: string;
  description: string;
  desiredDate: string | null;
  status: RequestStatus;
  createdAt: string;
  updatedAt: string;
}

/**
 * Datos de contacto del profesional. Disponibles solo cuando la solicitud está ACCEPTED.
 */
export interface ProfessionalContact {
  professionalName: string;
  phone: string;
  whatsapp: string | null;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
}
