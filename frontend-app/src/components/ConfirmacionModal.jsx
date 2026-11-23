export default function ConfirmacionModal({ titulo, mensaje, textoConfirmar, alCancelar, alConfirmar, cargando = false }) {
  return (
    <div className="modal-backdrop-custom" role="presentation" onMouseDown={(evento) => {
      if (evento.target === evento.currentTarget && !cargando) alCancelar();
    }}>
      <section className="confirm-modal" role="dialog" aria-modal="true" aria-labelledby="confirm-title">
        <div className="modal-icon-warning"><i className="bi bi-exclamation-triangle" aria-hidden="true" /></div>
        <h2 id="confirm-title">{titulo}</h2>
        <p>{mensaje}</p>
        <div className="modal-actions">
          <button className="btn btn-light" type="button" disabled={cargando} onClick={alCancelar}>
            Cancelar
          </button>
          <button className="btn btn-danger" type="button" disabled={cargando} onClick={alConfirmar}>
            {cargando && <span className="spinner-border spinner-border-sm me-2" aria-hidden="true" />}
            {textoConfirmar}
          </button>
        </div>
      </section>
    </div>
  );
}
