import { useMemo, useState } from 'react';

export default function TablaDatos({ columnas, filas, claveFila, textoVacio = 'No hay registros para mostrar.' }) {
  const [columnaOrden, setColumnaOrden] = useState(columnas[0]?.clave);
  const [direccion, setDireccion] = useState('asc');
  const [pagina, setPagina] = useState(1);
  const [filasPorPagina, setFilasPorPagina] = useState(10);

  const filasOrdenadas = useMemo(() => [...filas].sort((a, b) => {
    const valorA = columnas.find((columna) => columna.clave === columnaOrden)?.valorOrden
      ? columnas.find((columna) => columna.clave === columnaOrden).valorOrden(a)
      : a[columnaOrden];
    const valorB = columnas.find((columna) => columna.clave === columnaOrden)?.valorOrden
      ? columnas.find((columna) => columna.clave === columnaOrden).valorOrden(b)
      : b[columnaOrden];
    const comparacion = String(valorA ?? '').localeCompare(String(valorB ?? ''), 'es', {
      numeric: true,
      sensitivity: 'base',
    });
    return direccion === 'asc' ? comparacion : -comparacion;
  }), [filas, columnas, columnaOrden, direccion]);

  const totalPaginas = Math.max(1, Math.ceil(filasOrdenadas.length / filasPorPagina));
  const paginaActual = Math.min(pagina, totalPaginas);
  const inicio = (paginaActual - 1) * filasPorPagina;
  const filasVisibles = filasOrdenadas.slice(inicio, inicio + filasPorPagina);

  function ordenarPor(clave) {
    if (!clave) return;
    setDireccion((actual) => (columnaOrden === clave && actual === 'asc' ? 'desc' : 'asc'));
    setColumnaOrden(clave);
    setPagina(1);
  }

  return (
    <>
      <div className="table-responsive">
        <table className="table data-table align-middle">
          <thead>
            <tr>
              {columnas.map((columna) => (
                <th key={columna.clave || columna.titulo} scope="col">
                  {columna.clave ? (
                    <button className="sort-button" type="button" onClick={() => ordenarPor(columna.clave)}>
                      {columna.titulo}
                      <i className={`bi ${columnaOrden === columna.clave ? (direccion === 'asc' ? 'bi-arrow-down' : 'bi-arrow-up') : 'bi-arrow-down-up'}`} aria-hidden="true" />
                    </button>
                  ) : columna.titulo}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {filasVisibles.map((fila) => (
              <tr key={claveFila(fila)}>
                {columnas.map((columna) => (
                  <td key={columna.clave || columna.titulo}>
                    {columna.render ? columna.render(fila) : (fila[columna.clave] ?? '—')}
                  </td>
                ))}
              </tr>
            ))}
            {filas.length === 0 && (
              <tr><td className="empty-cell" colSpan={columnas.length}>{textoVacio}</td></tr>
            )}
          </tbody>
        </table>
      </div>
      <div className="table-footer">
        <span className="table-count">
          {filas.length === 0 ? '0 registros' : `${inicio + 1}–${Math.min(inicio + filasPorPagina, filas.length)} de ${filas.length}`}
        </span>
        <div className="table-pagination">
          <label className="page-size-label" htmlFor="page-size">Filas:</label>
          <select
            id="page-size"
            className="form-select form-select-sm page-size-select"
            value={filasPorPagina}
            onChange={(evento) => { setFilasPorPagina(Number(evento.target.value)); setPagina(1); }}
          >
            {[10, 25, 50].map((cantidad) => <option key={cantidad} value={cantidad}>{cantidad}</option>)}
          </select>
          <button className="btn btn-light btn-sm page-button" type="button" aria-label="Página anterior" disabled={paginaActual <= 1} onClick={() => setPagina((actual) => Math.max(1, actual - 1))}>
            <i className="bi bi-chevron-left" aria-hidden="true" />
          </button>
          <span className="page-number">{paginaActual} / {totalPaginas}</span>
          <button className="btn btn-light btn-sm page-button" type="button" aria-label="Página siguiente" disabled={paginaActual >= totalPaginas} onClick={() => setPagina((actual) => Math.min(totalPaginas, actual + 1))}>
            <i className="bi bi-chevron-right" aria-hidden="true" />
          </button>
        </div>
      </div>
    </>
  );
}
