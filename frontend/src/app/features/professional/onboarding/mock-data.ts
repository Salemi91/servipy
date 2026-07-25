import { CategoryOption, CityOption } from '../../../shared/models/professional.model';

/**
 * Datos mock para el onboarding del profesional.
 * Serán reemplazados por llamadas HTTP cuando el backend esté listo.
 */

export const MOCK_CATEGORIES: CategoryOption[] = [
  { id: 1, name: 'Electricidad', icon: '⚡', description: 'Instalaciones y reparaciones eléctricas' },
  { id: 2, name: 'Plomería', icon: '🔧', description: 'Reparaciones e instalaciones de agua y gas' },
  { id: 3, name: 'Limpieza', icon: '🧹', description: 'Servicios de limpieza del hogar y oficinas' },
  { id: 4, name: 'Pintura', icon: '🎨', description: 'Pintura interior y exterior' },
  { id: 5, name: 'Jardinería', icon: '🌱', description: 'Mantenimiento de jardines y espacios verdes' },
  { id: 6, name: 'Albañilería', icon: '🧱', description: 'Construcción y reparaciones de mampostería' },
  { id: 7, name: 'Carpintería', icon: '🪚', description: 'Muebles a medida y reparaciones de madera' },
];

export const MOCK_CITIES: CityOption[] = [
  { id: 1, name: 'Asunción' },
  { id: 2, name: 'San Lorenzo' },
  { id: 3, name: 'Luque' },
  { id: 4, name: 'Fernando de la Mora' },
  { id: 5, name: 'Lambaré' },
  { id: 6, name: 'Capiatá' },
  { id: 7, name: 'Limpio' },
  { id: 8, name: 'Ñemby' },
  { id: 9, name: 'Mariano Roque Alonso' },
  { id: 10, name: 'Villa Elisa' },
];

/**
 * Sugerencias de nombre de servicio por categoría.
 * Se muestran como placeholder cuando Juan selecciona una categoría.
 */
export const SERVICE_NAME_SUGGESTIONS: Record<number, string[]> = {
  1: ['Instalación de tomacorrientes', 'Reparación de tablero eléctrico', 'Cableado completo', 'Instalación de luminarias'],
  2: ['Reparación de cañerías', 'Instalación de grifo', 'Destape de desagüe', 'Instalación de termocalefón'],
  3: ['Limpieza profunda del hogar', 'Limpieza de oficina', 'Limpieza post-obra', 'Limpieza de vidrios'],
  4: ['Pintura de interior', 'Pintura de exterior', 'Pintura decorativa', 'Impermeabilización'],
  5: ['Corte de césped', 'Poda de árboles', 'Diseño de jardín', 'Mantenimiento mensual'],
  6: ['Revoque y alisado', 'Construcción de pared', 'Reparación de techo', 'Contrapiso'],
  7: ['Mueble a medida', 'Reparación de puerta', 'Instalación de placard', 'Restauración de mueble'],
};
