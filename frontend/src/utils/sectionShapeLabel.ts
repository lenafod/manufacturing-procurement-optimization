import type { SectionShape } from '../types';

// isti prevodi kao SectionShape.getDisplayName() na backend-u - sam enum ostaje na engleskom
// (vec je upisan u bazu preko Liquibase-a), ovo je samo prikazni sloj
const SECTION_SHAPE_LABELS: Record<SectionShape, string> = {
  ROUND: 'Okrugli',
  RECTANGULAR: 'Pravougaoni',
  HEXAGONAL: 'Šestougaoni',
  PIPE: 'Cevasti',
  CUBE: 'Kvadratni',
};

export function sectionShapeLabel(shape: SectionShape): string {
  return SECTION_SHAPE_LABELS[shape];
}
