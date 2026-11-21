export default function DetalleRegistroModal({ titulo, campos = [], cargando = false, alCerrar }) {
  return (
    <div className="modal-backdrop-custom" role="presentation" onMouseDown={(evento) => {
      if (evento.target === evento.currentTarget) alCerrar();
    }}>
      <section className="form-modal" role="dialog" aria-modal="true" aria-labelledby="detalle-registro-titulo">
        <div className="form-modal-heading">
          <div><p className="eyebrow">DETALLE CONSULTADO EN LA API</p><h2 id="detalle-registro-titulo">{titulo}</h2></div>
          <button className="icon-button" type="button" aria-label="Cerrar detalle" onClick={alCerrar}>
            <i className="bi bi-x-lg" aria-hidden="true" />
          </button>
        </div>
        {cargando ? <div className="py-4 text-center"><span className="spinner-border" role="status"><span className="visually-hidden">Cargando…</span></span></div> : (
          <dl className="row mb-0">
            {campos.map(({ etiqueta, valor }) => (
              <div className="col-12 col-md-6 mb-3" key={etiqueta}>
                <dt className="text-secondary small">{etiqueta}</dt>
                <dd className="mb-0 text-break">{valor ?? '—'}</dd>
              </div>
            ))}
          </dl>
        )}
        <div className="modal-actions"><button className="btn btn-light" type="button" onClick={alCerrar}>Cerrar</button></div>
      </section>
    </div>
  );
}
