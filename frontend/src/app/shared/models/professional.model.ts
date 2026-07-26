export interface ProfessionalSummary {
  id: number;
  name: string;
  professionalTitle: string;
  categoryName: string;
  description: string;
  cityName: string;
  referencePrice: number;
  availability: string;
  photoUrl: string | null;
}

export interface ProfessionalDetail {
  id: number;
  name: string;
  photoUrl: string | null;
  phone: string;
  whatsapp: string | null;
  description: string;
  cityName: string;
  availability: string;
  services: OfferedServiceItem[];
}

export interface OfferedServiceItem {
  id: number;
  name: string;
  description: string;
  price: number;
  currency: string;
  categoryName: string;
}
