import { useEffect, useMemo, useState } from 'react';
import { obtenerEquipos } from '../api/equiposApi.js';
import { crearRevision, obtenerRevisiones, reprogramarRevision } from '../api/revisionesApi.js';
import EstadoCarga from '../components/EstadoCarga.jsx';
import TablaDatos from '../components/TablaDatos.jsx';
import Toast from '../components/Toast.jsx';

const inicial = { idRevision: '', equipoId: '', fechaProgramada: '', diasAvisoPrevio: '7', emailSupervisor: '' };
const fechaLocal = (valor) => valor ? new Intl.DateTimeFormat('es-CO', { dateStyle: 'short', timeZone: 'UTC' }).format(new Date(`${valor}T00:00:00Z`)) : '—';

export default function RevisionesPage() {
  const [revisiones, setRevisiones] = useState([]);
  const [equipos, setEquipos] = useState([]);
  const [busqueda, setBusqueda] = useState('');
  const [cargando, setCargando] = useState(true);
  const [errorCarga, setErrorCarga] = useState('');
  const [aviso, setAviso] = useState(null);
  const [modalCrear, setModalCrear] = useState(false);
  const [formulario, setFormulario] = useState(inicial);
  const [revisionEditar, setRevisionEditar] = useState(null);
  const [nuevaFecha, setNuevaFecha] = useState('');
  const [guardando, setGuardando] = useState(false);

  async function cargar() {
    setCargando(true); setErrorCarga('');
    try { const [lista, inventario] = await Promise.all([obtenerRevisiones(), obtenerEquipos()]); setRevisiones(lista); setEquipos(inventario); }
    catch (error) { setErrorCarga(error.message); }
    finally { setCargando(false); }
  }
  useEffect(() => { cargar(); }, []);

  const filtradas = useMemo(() => revisiones.filter((revision) => [revision.idRevision, revision.equipo?.idEquipo, revision.equipo?.nombre, revision.emailSupervisor, revision.fechaProgramada]
    .some((dato) => String(dato ?? '').toLocaleLowerCase('es-CO').includes(busqueda.trim().toLocaleLowerCase('es-CO')))), [revisiones, busqueda]);

  async function guardar(evento) {
    evento.preventDefault(); setGuardando(true);
    try {
      const mensaje = await crearRevision({ ...formulario, diasAvisoPrevio: Number(formulario.diasAvisoPrevio) });
      setModalCrear(false); setFormulario(inicial); await cargar(); setAviso({ tipo: 'exito', mensaje: typeof mensaje === 'string' ? mensaje : 'La revisión quedó programada.' });
    } catch (error) { setAviso({ tipo: 'error', mensaje: error.message }); }
    finally { setGuardando(false); }
  }

  async function guardarReprogramacion(evento) {
    evento.preventDefault(); if (!revisionEditar) return;
    setGuardando(true);
    try {
      const mensaje = await reprogramarRevision(revisionEditar.idRevision, nuevaFecha);
      setRevisionEditar(null); await cargar(); setAviso({ tipo: 'exito', mensaje: typeof mensaje === 'string' ? mensaje : 'La revisión fue reprogramada y su alerta se reinició.' });
    } catch (error) { setAviso({ tipo: 'error', mensaje: error.message }); }
    finally { setGuardando(false); }
  }

  const columnas = [
    { clave: 'idRevision', titulo: 'Código' },
    { clave: 'equipo', titulo: 'Equipo', valorOrden: (item) => item.equipo?.idEquipo, render: (item) => <><strong>{item.equipo?.idEquipo || '—'}</strong><small className="d-block text-secondary">{item.equipo?.nombre || ''}</small></> },
    { clave: 'fechaProgramada', titulo: 'Fecha programada', render: (item) => fechaLocal(item.fechaProgramada) },
    { clave: 'diasAvisoPrevio', titulo: 'Aviso previo', render: (item) => `${item.diasAvisoPrevio} días` },
    { clave: 'emailSupervisor', titulo: 'Supervisor' },
    { clave: 'alertaGenerada', titulo: 'Alerta', render: (item) => <span className={`status-badge ${item.alertaGenerada ? 'status-operativo' : 'status-neutral'}`}>{item.alertaGenerada ? 'Generada' : 'Pendiente'}</span> },
    { titulo: 'Acción', render: (item) => <button className="btn btn-outline-primary btn-sm" type="button" onClick={() => { setRevisionEditar(item); setNuevaFecha(item.fechaProgramada); }}><i className="bi bi-calendar2-week me-1" aria-hidden="true" />Reprogramar</button> },
  ];
  const equiposActivos = equipos.filter((equipo) => !equipo.fueraDeServicio && equipo.estado?.toLocaleLowerCase('es-CO') !== 'fuera de servicio');

  return <section className="page-section">
    <Toast aviso={aviso} alCerrar={() => setAviso(null)} />
    <div className="page-heading"><div><p className="eyebrow">PLANIFICACIÓN</p><h1>Revisiones preventivas</h1><p className="page-description">Programa mantenimientos, define anticipación de alertas y reprograma fechas.</p></div><button className="btn btn-primary primary-action" type="button" onClick={() => setModalCrear(true)}><i className="bi bi-calendar-plus me-2" aria-hidden="true" />Programar revisión</button></div>
    <div className="table-card"><div className="table-toolbar"><div className="search-control"><i className="bi bi-search" aria-hidden="true" /><input className="form-control" type="search" aria-label="Buscar revisiones" placeholder="Buscar por código, equipo o supervisor" value={busqueda} onChange={(event) => setBusqueda(event.target.value)} /></div><span className="result-count">{filtradas.length} revisiones</span></div>
      {cargando ? <EstadoCarga mensaje="Cargando revisiones…" /> : errorCarga ? <div className="load-error" role="alert"><span>{errorCarga}</span><button className="btn btn-outline-primary btn-sm" type="button" onClick={cargar}>Reintentar</button></div> : <TablaDatos columnas={columnas} filas={filtradas} claveFila={(item) => item.idRevision} textoVacio={revisiones.length ? 'No se encontraron revisiones.' : 'Todavía no hay revisiones programadas.'} />}
    </div>
    {modalCrear && <div className="modal-backdrop-custom" role="presentation"><section className="form-modal" role="dialog" aria-modal="true" aria-labelledby="revision-modal-title"><div className="form-modal-heading"><div><p className="eyebrow">MANTENIMIENTO PREVENTIVO</p><h2 id="revision-modal-title">Programar revisión</h2></div><button className="icon-button" type="button" aria-label="Cerrar formulario" onClick={() => setModalCrear(false)}><i className="bi bi-x-lg" aria-hidden="true" /></button></div><form onSubmit={guardar}><div className="form-row"><div className="form-field"><label htmlFor="rev-id">Código de revisión <span>*</span></label><input id="rev-id" className="form-control" name="idRevision" maxLength="30" required value={formulario.idRevision} onChange={(event) => setFormulario({ ...formulario, idRevision: event.target.value })} /></div><div className="form-field"><label htmlFor="rev-equipo">Equipo <span>*</span></label><select id="rev-equipo" className="form-select" required value={formulario.equipoId} onChange={(event) => setFormulario({ ...formulario, equipoId: event.target.value })}><option value="">Selecciona</option>{equiposActivos.map((equipo) => <option key={equipo.idEquipo} value={equipo.idEquipo}>{equipo.idEquipo} · {equipo.nombre}</option>)}</select></div></div><div className="form-row"><div className="form-field"><label htmlFor="rev-fecha">Fecha programada <span>*</span></label><input id="rev-fecha" className="form-control" type="date" required value={formulario.fechaProgramada} onChange={(event) => setFormulario({ ...formulario, fechaProgramada: event.target.value })} /></div><div className="form-field"><label htmlFor="rev-dias">Aviso anticipado (días) <span>*</span></label><input id="rev-dias" className="form-control" type="number" min="0" max="365" required value={formulario.diasAvisoPrevio} onChange={(event) => setFormulario({ ...formulario, diasAvisoPrevio: event.target.value })} /></div></div><div className="form-field"><label htmlFor="rev-email">Correo del supervisor <span>*</span></label><input id="rev-email" className="form-control" type="email" maxLength="254" required value={formulario.emailSupervisor} onChange={(event) => setFormulario({ ...formulario, emailSupervisor: event.target.value })} /></div><div className="initial-state-note"><i className="bi bi-info-circle" aria-hidden="true" />El motor verifica las alertas automáticamente y genera una solicitud al llegar la fecha de aviso.</div><div className="modal-actions"><button className="btn btn-light" type="button" onClick={() => setModalCrear(false)}>Cancelar</button><button className="btn btn-primary" type="submit" disabled={guardando || equiposActivos.length === 0}>{guardando && <span className="spinner-border spinner-border-sm me-2" aria-hidden="true" />}Guardar revisión</button></div></form></section></div>}
    {revisionEditar && <div className="modal-backdrop-custom" role="presentation"><section className="form-modal" role="dialog" aria-modal="true" aria-labelledby="reprogramar-title"><div className="form-modal-heading"><div><p className="eyebrow">REVISIÓN {revisionEditar.idRevision}</p><h2 id="reprogramar-title">Reprogramar fecha</h2></div><button className="icon-button" type="button" aria-label="Cancelar reprogramación" onClick={() => setRevisionEditar(null)}><i className="bi bi-x-lg" aria-hidden="true" /></button></div><form onSubmit={guardarReprogramacion}><div className="form-field"><label htmlFor="rev-nueva-fecha">Nueva fecha <span>*</span></label><input id="rev-nueva-fecha" className="form-control" type="date" required value={nuevaFecha} onChange={(event) => setNuevaFecha(event.target.value)} /></div><div className="initial-state-note"><i className="bi bi-arrow-repeat" aria-hidden="true" />Al reprogramar, la alerta se reinicia y se vuelve a evaluar automáticamente.</div><div className="modal-actions"><button className="btn btn-light" type="button" onClick={() => setRevisionEditar(null)}>Cancelar</button><button className="btn btn-primary" type="submit" disabled={guardando}>{guardando && <span className="spinner-border spinner-border-sm me-2" aria-hidden="true" />}Guardar fecha</button></div></form></section></div>}
  </section>;
}
