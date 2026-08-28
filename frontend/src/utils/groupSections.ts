import type { MaterialSectionType } from '../types';
import { sectionShapeLabel } from './sectionShapeLabel';
import type { SelectOptionGroup } from '../components/Select';

// grupise preseke po obliku (Okrugli/Pravougaoni/...) sa dimenzijama kao podlistom - oblici
// azbucno, dimenzije unutar svakog oblika rastuce po dim1
export function groupSectionsByShape(sections: MaterialSectionType[]): SelectOptionGroup[] {
  const byShape = new Map<string, MaterialSectionType[]>();

  for (const section of sections) {
    const label = sectionShapeLabel(section.typeName);
    const group = byShape.get(label);
    if (group) {
      group.push(section);
    } else {
      byShape.set(label, [section]);
    }
  }

  return Array.from(byShape.entries())
    .sort(([a], [b]) => a.localeCompare(b))
    .map(([label, group]) => ({
      label,
      options: group
        .slice()
        .sort((a, b) => a.dim1 - b.dim1)
        .map((s) => ({
          value: String(s.id),
          label: s.usesDim2 && s.dim2 != null ? `${s.dim1}×${s.dim2}` : `${s.dim1}`,
        })),
    }));
}
