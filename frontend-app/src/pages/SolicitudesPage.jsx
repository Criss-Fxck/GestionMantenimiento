import { useEffect, useMemo, useState } from 'react';
import { obtenerEquipos } from '../api/equiposApi.js';
import { cerrarSolicitud, crearSolicitud, obtenerSolicitudes } from '../api/solicitudesApi.js';
import ConfirmacionModal from '../components/ConfirmacionModal.jsx';
import EstadoCarga from '../components/EstadoCarga.jsx';
import TablaDatos from '../components/TablaDatos.jsx';
import Toast from '../components/Toast.jsx';

const formularioInicial = {
  equipoId: '', solicitanteId: '', solicitanteNombre: '', departamento: '', email: '', descripcion: '',
};

function mostrarFecha(valor) {
  if (!valor) return '—';
  const fecha = new Date(valor);
  return Number.isNaN(fecha.getTime()) ? valor : new Intl.DateTimeFormat('es-CO', { dateStyle: 'short', timeStyle: 'short', hour12: false }).format(fecha);
}

function estadoVisual(estado) {
  const normalizado = String(estado || '').toLocaleLowerCase('es-CO');
  if (normalizado === 'cerrado') return ['Cerrado', 'status-operativo'];
  if (normalizado === 'asignado') return ['Asignado', 'status-neutral'];
  return ['Reportado', 'status-critico'];
}

export default function SolicitudesPage() {
  const [solicitudes, setSolicitudes] = useState([]);
  const [equipos, setEquipos] = useState([]);
  const [busqueda, setBusqueda] = useState('');
  const [filtroEstado, setFiltroEstado] = useState('todos');
  const [cargando, setCargando] = useState(true);
  const [errorCarga, setErrorCarga] = useState('');
  const [aviso, setAviso] = useState(null);
  const [mostrarFormulario, setMostrarFormulario] = useState(false);
  const [formulario, setFormulario] = useState(formularioInicial);
  const [solicitudEnCierre, setSolicitudEnCierre] = useState(null);
  const [confirmacionCierre, setConfirmacionCierre] = useState(null);
  const [guardando, setGuardando] = useState(false);

  async function cargarDatos() {
    setCargando(true);
    setErrorCarga('');
    try {
      const [listaSolicitudes, listaEquipos] = await Promise.all([obtenerSolicitudes(), obtenerEquipos()]);
      setSolicitudes(listaSolicitudes);
      setEquipos(listaEquipos);
    } catch (error) {
      setErrorCarga(error.message);
    } finally {
      setCargando(false);
    }
  }

  useEffect(() => { cargarDatos(); }, []);

  const solicitudesFiltradas = useMemo(() => solicitudes.filter((solicitud) => {
    const term = busqueda.trim().toLocaleLowerCase('es-CO');
    const coincideTexto = !term || [solicitud.idSolicitud, solicitud.descripcionFalla, solicitud.estado,
      solicitud.equipo?.idEquipo, solicitud.equipo?.nombre, solicitud.solicitante?.nombre,
      solicitud.tecnicoAsignado?.nombre].some((dato) => String(dato ?? '').toLocaleLowerCase('es-CO').includes(term));
    return coincideTexto && (filtroEstado === 'todos' || solicitud.estado?.toLocaleLowerCase('es-CO') === filtroEstado);
  }), [solicitudes, busqueda, filtroEstado]);

  function cambiarCampo(evento) {
    setFormulario((actual) => ({ ...actual, [evento.target.name]: evento.target.value }));
  }

  async function guardarSolicitud(evento) {
    evento.preventDefault();
    setGuardando(true);
    try {
      const equipo = equipos.find((item) => item.idEquipo === formulario.equipoId);
      const creada = await crearSolicitud({
        equipoId: formulario.equipoId,
        equipoNombre: equipo?.nombre || '',
        equipoUbicacion: equipo?.ubicacion || '',
        solicitanteId: formulario.solicitanteId.trim(),
        solicitanteNombre: formulario.solicitanteNombre.trim(),
        departamento: formulario.departamento.trim(),
        email: formulario.email.trim(),
        descripcion: formulario.descripcion.trim(),
      });
      setSolicitudes((actuales) => [creada, ...actuales]);
      setMostrarFormulario(false);
      setFormulario(formularioInicial);
      setAviso({ tipo: 'exito', mensaje: `Solicitud #${creada.idSolicitud} creada y asignada a ${creada.tecnicoAsignado?.nombre || 'un técnico'}. Se envió el correo al solicitante.` });
    } catch (error) {
      setAviso({ tipo: 'error', mensaje: error.message });
    } finally {
      setGuardando(false);
    }
  }

  async function confirmarCierre() {
    if (!confirmacionCierre) return;
    setGuardando(true);
    try {
      const cerrada = await cerrarSolicitud(confirmacionCierre.idSolicitud, {
        diagnostico: confirmacionCierre.diagnostico.trim(),
        solucion: confirmacionCierre.solucion.trim(),
      });
      setSolicitudes((actuales) => actuales.map((item) => item.idSolicitud === cerrada.idSolicitud ? cerrada : item));
      setSolicitudEnCierre(null);
      setConfirmacionCierre(null);
      setAviso({ tipo: 'exito', mensaje: `La solicitud #${cerrada.idSolicitud} quedó cerrada y se notificó al solicitante.` });
    } catch (error) {
      setAviso({ tipo: 'error', mensaje: error.message });
    } finally {
      setGuardando(false);
    }
  }

  const columnas = [
    { clave: 'idSolicitud', titulo: 'N.º', render: (item) => `#${item.idSolicitud}` },
    { clave: 'equipo', titulo: 'Equipo', valorOrden: (item) => item.equipo?.idEquipo, render: (item) => <><strong>{item.equipo?.idEquipo || '—'}</strong><small className="d-block text-secondary">{item.equipo?.nombre || ''}</small></> },
    { clave: 'descripcionFalla', titulo: 'Descripción', render: (item) => <span title={item.descripcionFalla}>{item.descripcionFalla?.length > 58 ? `${item.descripcionFalla.slice(0, 58)}…` : item.descripcionFalla}</span> },
    { clave: 'solicitante', titulo: 'Solicitante', valorOrden: (item) => item.solicitante?.nombre, render: (item) => item.solicitante?.nombre || '—' },
    { clave: 'tecnicoAsignado', titulo: 'Técnico', valorOrden: (item) => item.tecnicoAsignado?.nombre, render: (item) => item.tecnicoAsignado?.nombre || '—' },
    { clave: 'estado', titulo: 'Estado', render: (item) => { const [texto, clase] = estadoVisual(item.estado); return <span className={`status-badge ${clase}`}>{texto}</span>; } },
    { clave: 'fechaHoraCreacion', titulo: 'Creada', render: (item) => mostrarFecha(item.fechaHoraCreacion) },
    { titulo: 'Acciones', render: (item) => item.estado?.toLocaleLowerCase('es-CO') === 'cerrado'
      ? <span className="action-completed">Cerrada</span>
      : <button className="btn btn-outline-primary btn-sm" type="button" onClick={() => setSolicitudEnCierre(item)}><i className="bi bi-check2-circle me-1" aria-hidden="true" />Cerrar</button> },
  ];

  const equiposDisponibles = equipos.filter((equipo) => !equipo.fueraDeServicio && equipo.estado?.toLocaleLowerCase('es-CO') !== 'fuera de servicio');

  return (
    <section className="page-section">
      <Toast aviso={aviso} alCerrar={() => setAviso(null)} />
      <div className="page-heading">
        <div><p className="eyebrow">MANTENIMIENTO CORRECTIVO</p><h1>Solicitudes</h1><p className="page-description">Registra fallas, consulta su asignación y documenta el cierre.</p></div>
        <button className="btn btn-primary primary-action" type="button" onClick={() => setMostrarFormulario(true)}><i className="bi bi-plus-lg me-2" aria-hidden="true" />Nueva solicitud</button>
      </div>
      <div className="table-card">
        <div className="table-toolbar">
          <div className="search-control"><i className="bi bi-search" aria-hidden="true" /><input className="form-control" type="search" aria-label="Buscar solicitudes" placeholder="Buscar por número, equipo o persona" value={busqueda} onChange={(event) => setBusqueda(event.target.value)} /></div>
          <label className="filter-control"><span>Estado</span><select className="form-select" value={filtroEstado} onChange={(event) => setFiltroEstado(event.target.value)}><option value="todos">Todos</option><option value="reportado">Reportado</option><option value="asignado">Asignado</option><option value="cerrado">Cerrado</option></select></label>
          <span className="result-count">{solicitudesFiltradas.length} solicitudes</span>
        </div>
        {cargando ? <EstadoCarga mensaje="Cargando solicitudes…" /> : errorCarga ? <div className="load-error" role="alert"><span>{errorCarga}</span><button className="btn btn-outline-primary btn-sm" type="button" onClick={cargarDatos}>Reintentar</button></div> : <TablaDatos columnas={columnas} filas={solicitudesFiltradas} claveFila={(item) => item.idSolicitud} textoVacio={solicitudes.length ? 'No hay solicitudes con esos criterios.' : 'Todavía no hay solicitudes registradas.'} />}
      </div>

      {mostrarFormulario && <div className="modal-backdrop-custom" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget && !guardando) setMostrarFormulario(false); }}>
        <section className="form-modal" role="dialog" aria-modal="true" aria-labelledby="solicitud-modal-title"><div className="form-modal-heading"><div><p className="eyebrow">NUEVO REPORTE</p><h2 id="solicitud-modal-title">Registrar solicitud</h2></div><button className="icon-button" type="button" aria-label="Cerrar formulario" onClick={() => setMostrarFormulario(false)}><i className="bi bi-x-lg" aria-hidden="true" /></button></div>
          <form onSubmit={guardarSolicitud}>
            <div className="form-field"><label htmlFor="sol-equipo">Equipo <span>*</span></label><select id="sol-equipo" className="form-select" name="equipoId" required value={formulario.equipoId} onChange={cambiarCampo}><option value="">Selecciona un equipo</option>{equiposDisponibles.map((equipo) => <option key={equipo.idEquipo} value={equipo.idEquipo}>{equipo.idEquipo} · {equipo.nombre} — {equipo.ubicacion}</option>)}</select>{equiposDisponibles.length === 0 && <small className="text-danger">No hay equipos operativos registrados.</small>}</div>
            <div className="form-row"><div className="form-field"><label htmlFor="sol-id">ID del solicitante <span>*</span></label><input id="sol-id" className="form-control" name="solicitanteId" maxLength="40" required value={formulario.solicitanteId} onChange={cambiarCampo} /></div><div className="form-field"><label htmlFor="sol-nombre">Nombre <span>*</span></label><input id="sol-nombre" className="form-control" name="solicitanteNombre" maxLength="120" required value={formulario.solicitanteNombre} onChange={cambiarCampo} /></div></div>
            <div className="form-row"><div className="form-field"><label htmlFor="sol-departamento">Departamento <span>*</span></label><input id="sol-departamento" className="form-control" name="departamento" maxLength="100" required value={formulario.departamento} onChange={cambiarCampo} /></div><div className="form-field"><label htmlFor="sol-email">Correo <span>*</span></label><input id="sol-email" className="form-control" type="email" name="email" maxLength="254" required value={formulario.email} onChange={cambiarCampo} /></div></div>
            <div className="form-field"><label htmlFor="sol-descripcion">Descripción de la falla <span>*</span></label><textarea id="sol-descripcion" className="form-control" name="descripcion" rows="4" maxLength="2000" required value={formulario.descripcion} onChange={cambiarCampo} /></div>
            <div className="initial-state-note"><i className="bi bi-info-circle" aria-hidden="true" />Al guardar, el backend asigna el técnico con menor carga y envía un correo al solicitante.</div>
            <div className="modal-actions"><button className="btn btn-light" type="button" disabled={guardando} onClick={() => setMostrarFormulario(false)}>Cancelar</button><button className="btn btn-primary" type="submit" disabled={guardando || equiposDisponibles.length === 0}>{guardando && <span className="spinner-border spinner-border-sm me-2" aria-hidden="true" />}Crear solicitud</button></div>
          </form>
        </section>
      </div>}

      {solicitudEnCierre && <div className="modal-backdrop-custom" role="presentation"><section className="form-modal" role="dialog" aria-modal="true" aria-labelledby="cierre-title"><div className="form-modal-heading"><div><p className="eyebrow">SOLICITUD #{solicitudEnCierre.idSolicitud}</p><h2 id="cierre-title">Documentar cierre</h2></div><button className="icon-button" type="button" aria-label="Cancelar cierre" onClick={() => setSolicitudEnCierre(null)}><i className="bi bi-x-lg" aria-hidden="true" /></button></div><form onSubmit={(event) => { event.preventDefault(); const datos = new FormData(event.currentTarget); setConfirmacionCierre({ idSolicitud: solicitudEnCierre.idSolicitud, diagnostico: datos.get('diagnostico'), solucion: datos.get('solucion') }); }}><div className="form-field"><label htmlFor="cierre-diagnostico">Diagnóstico <span>*</span></label><textarea id="cierre-diagnostico" className="form-control" name="diagnostico" rows="3" maxLength="2000" required /></div><div className="form-field"><label htmlFor="cierre-solucion">Solución aplicada <span>*</span></label><textarea id="cierre-solucion" className="form-control" name="solucion" rows="3" maxLength="2000" required /></div><p className="text-secondary small">Al confirmar, se enviará el correo de cierre antes de guardar los cambios.</p><div className="modal-actions"><button className="btn btn-light" type="button" onClick={() => setSolicitudEnCierre(null)}>Cancelar</button><button className="btn btn-primary" type="submit">Continuar</button></div></form></section></div>}
      {confirmacionCierre && <ConfirmacionModal titulo={`Cerrar solicitud #${confirmacionCierre.idSolicitud}`} mensaje="Se notificará al solicitante y se liberará la carga del técnico asignado. ¿Confirmas el cierre?" textoConfirmar="Confirmar cierre" cargando={guardando} alCancelar={() => setConfirmacionCierre(null)} alConfirmar={confirmarCierre} />}
    </section>
  );
}
