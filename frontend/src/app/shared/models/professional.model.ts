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

/**
 * Detalle público del profesional. Sin datos de contacto: se obtienen
 * desde la solicitud del cliente cuando está ACCEPTED.
 */
export interface ProfessionalDetail {
  id: number;
  name: string;
  photoUrl: string | null;
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

// --- Onboarding / Profile Completion ---

export interface ProfessionalProfileForm {
  phone: string;
  whatsapp: string;
  description: string;
  city: string;
  availability?: string;
  photoUrl?: string | null;
}

export interface OfferedServiceForm {
  categoryId: number;
  name: string;
  description: string;
  price: number;
}

export interface ProfessionalProfileStatus {
  hasProfile: boolean;
  profileId: number | null;
  approvalStatus: string | null;
}
