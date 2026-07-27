export interface ProfessionalAdmin {
  id: number;
  name: string;
  email: string;
  phone: string;
  description: string;
  approvalStatus: 'PENDING' | 'APPROVED' | 'REJECTED';
  createdAt: string;
}
