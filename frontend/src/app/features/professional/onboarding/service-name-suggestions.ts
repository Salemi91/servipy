/**
 * Sugerencias de nombre de servicio por categoría, indexadas por nombre
 * de categoría en minúsculas. Son textos de ayuda de la interfaz,
 * no datos del backend.
 */
const SUGGESTIONS: Record<string, string[]> = {
  electricidad: [
    'Instalación de tomacorrientes',
    'Reparación de tablero eléctrico',
    'Cableado completo',
    'Instalación de luminarias',
  ],
  plomería: [
    'Reparación de cañerías',
    'Instalación de grifo',
    'Destape de desagüe',
    'Instalación de termocalefón',
  ],
  limpieza: [
    'Limpieza profunda del hogar',
    'Limpieza de oficina',
    'Limpieza post-obra',
    'Limpieza de vidrios',
  ],
  pintura: ['Pintura de interior', 'Pintura de exterior', 'Pintura decorativa', 'Impermeabilización'],
  jardinería: ['Corte de césped', 'Poda de árboles', 'Diseño de jardín', 'Mantenimiento mensual'],
  albañilería: ['Revoque y alisado', 'Construcción de pared', 'Reparación de techo', 'Contrapiso'],
  carpintería: [
    'Mueble a medida',
    'Reparación de puerta',
    'Instalación de placard',
    'Restauración de mueble',
  ],
};

export function suggestionsForCategory(categoryName: string | null | undefined): string[] {
  if (!categoryName) return [];
  return SUGGESTIONS[categoryName.toLowerCase()] ?? [];
}
