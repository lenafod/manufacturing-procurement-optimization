import { ApiError } from '../api/client';

export function ErrorBanner({ error }: { error: unknown }) {
  if (!error) return null;

  const message =
    error instanceof ApiError ? error.message : error instanceof Error ? error.message : 'Nepoznata greška';

  return (
    <div className="error-banner" role="alert">
      <span className="icon">⚠</span>
      <span>{message}</span>
    </div>
  );
}
