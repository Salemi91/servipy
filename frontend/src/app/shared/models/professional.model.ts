/**
 * Interfaces para el onboarding y perfil del profesional.
 * Alineadas con el modelo de dominio (docs/DOMAIN_MODEL.md).
 */

export interface ProfessionalProfileForm {
  phone: string;
  whatsapp: string;
  city: string;
  description: string;
  photoUrl: string | null;
}

export interface OfferedServiceForm {
  categoryId: number | null;
  name: string;
  description: string;
  price: number | null;
  currency: string; // siempre "PYG"
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
