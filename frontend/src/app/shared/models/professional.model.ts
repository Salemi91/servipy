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

export interface CategoryOption {
  id: number;
  name: string;
  icon: string;
  description: string;
}

export interface CityOption {
  id: number;
  name: string;
}

export interface ProfessionalProfileStatus {
  hasProfile: boolean;
  profileId: number | null;
  approvalStatus: string | null;
}
