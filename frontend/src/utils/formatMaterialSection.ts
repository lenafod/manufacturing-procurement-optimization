import type { MaterialSectionType } from '../types';
import { sectionShapeLabel } from './sectionShapeLabel';

// npr. "Okrugli 30" ili "Pravougaoni 40×20" - svaki red u material_section_type je vec konkretna dimenzija.
export function formatMaterialSection(section: MaterialSectionType): string {
  const dims = section.usesDim2 && section.dim2 != null ? `${section.dim1}×${section.dim2}` : `${section.dim1}`;
  return `${sectionShapeLabel(section.typeName)} ${dims}`;
}
