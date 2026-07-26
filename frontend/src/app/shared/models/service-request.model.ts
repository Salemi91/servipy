export interface CreateServiceRequestPayload {
  name: string;
  email: string;
  phone?: string;
  subject: string;
  description: string;
  desiredDate?: string; // ISO date string YYYY-MM-DD
}

export interface CreateServiceRequestResponse {
  id: number;
}

export interface ServiceRequestSummary {
  id: number;
  clientName: string;
  subject: string;
  status: 'PENDING' | 'ACCEPTED' | 'REJECTED';
  createdAt: string; // ISO timestamp
}

export interface ServiceRequestDetail {
  id: number;
  clientName: string;
  clientEmail: string;
  clientPhone: string | null;
  subject: string;
  description: string;
  desiredDate: string | null;
  status: 'PENDING' | 'ACCEPTED' | 'REJECTED';
  createdAt: string;
  updatedAt: string;
}

export interface ChangeStatusPayload {
  status: 'ACCEPTED' | 'REJECTED';
}
