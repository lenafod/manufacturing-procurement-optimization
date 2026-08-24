import { useQuery } from '@tanstack/react-query';
import { LoadingState } from '../../components/LoadingState';
import { ErrorState } from '../../components/ErrorState';
import { materialTypesApi } from '../../api/materialTypes';
import { materialSectionTypesApi } from '../../api/materialSectionTypes';
import { machiningTypesApi } from '../../api/machiningTypes';
import { surfaceProtectionsApi } from '../../api/surfaceProtections';
import { technicalProcessingsApi } from '../../api/technicalProcessings';
import { sectionShapeLabel } from '../../utils/sectionShapeLabel';

// Ovo su seed-only šifarnici na backendu (bez @GeneratedValue, bez POST endpoint-a),
// pa je ovaj ekran samo za pregled - koriste se kao dropdown izvor u drugim formama.
export function ReferenceDataPage() {
  const materialTypes = useQuery({ queryKey: ['materialTypes'], queryFn: materialTypesApi.getAll });
  const materialSectionTypes = useQuery({
    queryKey: ['materialSectionTypes'],
    queryFn: materialSectionTypesApi.getAll,
  });
  const machiningTypes = useQuery({ queryKey: ['machiningTypes'], queryFn: machiningTypesApi.getAll });
  const surfaceProtections = useQuery({
    queryKey: ['surfaceProtections'],
    queryFn: surfaceProtectionsApi.getAll,
  });
  const technicalProcessings = useQuery({
    queryKey: ['technicalProcessings'],
    queryFn: technicalProcessingsApi.getAll,
  });

  return (
    <div>
      <h1>Šifarnici</h1>

      <section>
        <h2>Vrste materijala</h2>
        {materialTypes.isLoading && <LoadingState />}
        {materialTypes.isError && <ErrorState error={materialTypes.error} />}
        {materialTypes.data && (
          <ul>
            {materialTypes.data.map((m) => (
              <li key={m.id}>
                {m.materialName} (gustina: {m.density})
              </li>
            ))}
          </ul>
        )}
      </section>

      <section>
        <h2>Preseci</h2>
        {materialSectionTypes.isLoading && <LoadingState />}
        {materialSectionTypes.isError && <ErrorState error={materialSectionTypes.error} />}
        {materialSectionTypes.data && (
          <ul>
            {materialSectionTypes.data.map((s) => (
              <li key={s.id}>
                {sectionShapeLabel(s.typeName)} — dim1: {s.dim1}
                {s.usesDim2 ? `, dim2: ${s.dim2}` : ''}
              </li>
            ))}
          </ul>
        )}
      </section>

      <section>
        <h2>Mašinske obrade</h2>
        {machiningTypes.isLoading && <LoadingState />}
        {machiningTypes.isError && <ErrorState error={machiningTypes.error} />}
        {machiningTypes.data && (
          <ul>
            {machiningTypes.data.map((m) => (
              <li key={m.id}>{m.name}</li>
            ))}
          </ul>
        )}
      </section>

      <section>
        <h2>Površinske zaštite</h2>
        {surfaceProtections.isLoading && <LoadingState />}
        {surfaceProtections.isError && <ErrorState error={surfaceProtections.error} />}
        {surfaceProtections.data && (
          <ul>
            {surfaceProtections.data.map((s) => (
              <li key={s.id}>{s.name}</li>
            ))}
          </ul>
        )}
      </section>

      <section>
        <h2>Tehnička obrada</h2>
        {technicalProcessings.isLoading && <LoadingState />}
        {technicalProcessings.isError && <ErrorState error={technicalProcessings.error} />}
        {technicalProcessings.data && (
          <ul>
            {technicalProcessings.data.map((t) => (
              <li key={t.id}>{t.name}</li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}
