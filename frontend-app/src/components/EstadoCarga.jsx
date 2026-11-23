export default function EstadoCarga({ mensaje = 'Cargando información…' }) {
  return (
    <div className="loading-state" role="status" aria-live="polite">
      <span className="spinner-border spinner-border-sm" aria-hidden="true" />
      <span>{mensaje}</span>
    </div>
  );
}
