export function ErrorState({ error }: { error: unknown }) {
  const message = error instanceof Error ? error.message : 'Nepoznata greška';
  return <p role="alert" style={{ color: 'crimson' }}>Greška: {message}</p>;
}
