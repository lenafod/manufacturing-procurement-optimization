import { ErrorBanner } from './ErrorBanner';

export function ErrorState({ error }: { error: unknown }) {
  return <ErrorBanner error={error} />;
}
