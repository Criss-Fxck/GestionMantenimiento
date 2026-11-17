import { useEffect } from 'react';

export default function Toast({ aviso, alCerrar }) {
  useEffect(() => {
    if (!aviso) return undefined;
    const temporizador = window.setTimeout(alCerrar, 5000);
    return () => window.clearTimeout(temporizador);
  }, [aviso, alCerrar]);

  if (!aviso) return null;

  const esError = aviso.tipo === 'error';
  return (
    <div
      className={`app-toast ${esError ? 'app-toast-error' : 'app-toast-success'}`}
      role={esError ? 'alert' : 'status'}
      aria-live={esError ? 'assertive' : 'polite'}
    >
      <i className={`bi ${esError ? 'bi-exclamation-circle-fill' : 'bi-check-circle-fill'}`} aria-hidden="true" />
      <span>{aviso.mensaje}</span>
      <button type="button" className="toast-close" aria-label="Cerrar aviso" onClick={alCerrar}>
        <i className="bi bi-x-lg" aria-hidden="true" />
      </button>
    </div>
  );
}
