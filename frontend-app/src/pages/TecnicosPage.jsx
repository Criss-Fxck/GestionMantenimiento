import { useEffect, useMemo, useState } from 'react';
import { asignarCasoManual, crearTecnico, obtenerCargaTecnicos } from '../api/tecnicosApi.js';
import ConfirmacionModal from '../components/ConfirmacionModal.jsx';
import EstadoCarga from '../components/EstadoCarga.jsx';
import TablaDatos from '../components/TablaDatos.jsx';
import Toast from '../components/Toast.jsx';

const inicial = { idUsuario: '', nombre: '', departamento: '', especialidad: '' };

function estadoTecnico(estado) {
  if (estado === 'LIBRE') return ['Libre', 'status-operativo'];
  if (estado === 'OCUPADO_AL_LIMITE') return ['Ocupado al límite', 'status-fuera'];
  return ['Parcialmente ocupado', 'status-critico'];
}

export default function TecnicosPage() {
  const [tecnicos, setTecnicos] = useState([]);
  const [busqueda, setBusqueda] = useState('');
  const [cargando, setCargando] = useState(true);
  const [errorCarga, setErrorCarga] = useState('');
  const [aviso, setAviso] = useState(null);
  const [modalAbierto, setModalAbierto] = useState(false);
  const [formulario, setFormulario] = useState(inicial);
  const [tecnicoPorAsignar, setTecnicoPorAsignar] = useState(null);
  const [guardando, setGuardando] = useState(false);

  async function cargarTecnicos() {
    setCargando(true); setErrorCarga('');
    try { setTecnicos(await obtenerCargaTecnicos()); }
    catch (error) { setErrorCarga(error.message); }
    finally { setCargando(false); }
  }
  useEffect(() => { cargarTecnicos(); }, []);

  const filtrados = useMemo(() => tecnicos.filter((tecnico) => [tecnico.idUsuario, tecnico.nombre, tecnico.estado]
    .some((dato) => String(dato ?? '').toLocaleLowerCase('es-CO').includes(busqueda.trim().toLocaleLowerCase('es-CO')))), [tecnicos, busqueda]);

  async function guardar(evento) {
    evento.preventDefault(); setGuardando(true);
    try {
      await crearTecnico(Object.fromEntries(Object.entries(formulario).map(([clave, valor]) => [clave, valor.trim()])));
      setModalAbierto(false); setFormulario(inicial);
      await cargarTecnicos();
      setAviso({ tipo: 'exito', mensaje: 'El técnico quedó registrado.' });
    } catch (error) { setAviso({ tipo: 'error', mensaje: error.message }); }
    finally { setGuardando(false); }
  }

  async function asignarCaso() {
    if (!tecnicoPorAsignar) return;
    setGuardando(true);
    try {
      const tecnico = tecnicoPorAsignar;
      const mensaje = await asignarCasoManual(tecnico.idUsuario);
      setTecnicoPorAsignar(null);
      await cargarTecnicos();
      setAviso({ tipo: 'exito', mensaje: typeof mensaje === 'string' ? mensaje : `Se incrementó la carga de ${tecnico.nombre}.` });
    } catch (error) { setAviso({ tipo: 'error', mensaje: error.message }); }
    finally { setGuardando(false); }
  }

  const columnas = [
    { clave: 'idUsuario', titulo: 'ID' },
    { clave: 'nombre', titulo: 'Técnico' },
    { clave: 'casosActivos', titulo: 'Casos activos' },
    { clave: 'porcentajeOcupacion', titulo: 'Ocupación', valorOrden: (item) => item.casosActivos, render: (item) => <div className="occupancy-cell"><div className="progress occupancy-progress" role="progressbar" aria-label={`Ocupación de ${item.nombre}`} aria-valuenow={item.casosActivos * 20} aria-valuemin="0" aria-valuemax="100"><div className={`progress-bar ${item.casosActivos >= 5 ? 'bg-danger' : item.casosActivos >= 3 ? 'bg-warning' : 'bg-success'}`} style={{ width: `${Math.min(100, item.casosActivos * 20)}%` }} /></div><span>{item.porcentajeOcupacion}</span></div> },
    { clave: 'estado', titulo: 'Disponibilidad', render: (item) => { const [texto, clase] = estadoTecnico(item.estado); return <span className={`status-badge ${clase}`}>{texto}</span>; } },
    { titulo: 'Acción', render: (item) => <button className="btn btn-outline-primary btn-sm" type="button" disabled={item.casosActivos >= 5} title="Incrementa la carga manual del técnico; no crea una solicitud" onClick={() => setTecnicoPorAsignar(item)}><i className="bi bi-plus-circle me-1" aria-hidden="true" />Sumar caso</button> },
  ];

  return <section className="page-section">
    <Toast aviso={aviso} alCerrar={() => setAviso(null)} />
    <div className="page-heading"><div><p className="eyebrow">CAPACIDAD DEL EQUIPO</p><h1>Técnicos y carga</h1><p className="page-description">Consulta disponibilidad, ocupación y registra técnicos.</p></div><button className="btn btn-primary primary-action" type="button" onClick={() => setModalAbierto(true)}><i className="bi bi-person-plus me-2" aria-hidden="true" />Registrar técnico</button></div>
    <div className="table-card"><div className="table-toolbar"><div className="search-control"><i className="bi bi-search" aria-hidden="true" /><input className="form-control" type="search" aria-label="Buscar técnicos" placeholder="Buscar por ID o nombre" value={busqueda} onChange={(event) => setBusqueda(event.target.value)} /></div><span className="result-count">{filtrados.length} técnicos · máximo 5 casos por técnico</span></div>
      {cargando ? <EstadoCarga mensaje="Cargando técnicos…" /> : errorCarga ? <div className="load-error" role="alert"><span>{errorCarga}</span><button className="btn btn-outline-primary btn-sm" type="button" onClick={cargarTecnicos}>Reintentar</button></div> : <TablaDatos columnas={columnas} filas={filtrados} claveFila={(item) => item.idUsuario} textoVacio={tecnicos.length ? 'No se encontraron técnicos.' : 'Todavía no hay técnicos registrados.'} />}
    </div>
    {modalAbierto && <div className="modal-backdrop-custom" role="presentation"><section className="form-modal" role="dialog" aria-modal="true" aria-labelledby="tecnico-modal-title"><div className="form-modal-heading"><div><p className="eyebrow">EQUIPO DE MANTENIMIENTO</p><h2 id="tecnico-modal-title">Registrar técnico</h2></div><button className="icon-button" type="button" aria-label="Cerrar formulario" onClick={() => setModalAbierto(false)}><i className="bi bi-x-lg" aria-hidden="true" /></button></div><form onSubmit={guardar}><div className="form-field"><label htmlFor="tec-id">ID de usuario <span>*</span></label><input id="tec-id" className="form-control" name="idUsuario" maxLength="40" required value={formulario.idUsuario} onChange={(event) => setFormulario({ ...formulario, idUsuario: event.target.value })} /></div><div className="form-row"><div className="form-field"><label htmlFor="tec-nombre">Nombre <span>*</span></label><input id="tec-nombre" className="form-control" name="nombre" maxLength="120" required value={formulario.nombre} onChange={(event) => setFormulario({ ...formulario, nombre: event.target.value })} /></div><div className="form-field"><label htmlFor="tec-depto">Departamento <span>*</span></label><input id="tec-depto" className="form-control" name="departamento" maxLength="100" required value={formulario.departamento} onChange={(event) => setFormulario({ ...formulario, departamento: event.target.value })} /></div></div><div className="form-field"><label htmlFor="tec-especialidad">Especialidad <span>*</span></label><input id="tec-especialidad" className="form-control" name="especialidad" maxLength="120" required value={formulario.especialidad} onChange={(event) => setFormulario({ ...formulario, especialidad: event.target.value })} /></div><div className="initial-state-note"><i className="bi bi-info-circle" aria-hidden="true" />La carga máxima admitida es de cinco casos activos.</div><div className="modal-actions"><button className="btn btn-light" type="button" onClick={() => setModalAbierto(false)}>Cancelar</button><button className="btn btn-primary" type="submit" disabled={guardando}>{guardando && <span className="spinner-border spinner-border-sm me-2" aria-hidden="true" />}Guardar técnico</button></div></form></section></div>}
    {tecnicoPorAsignar && <ConfirmacionModal titulo="Sumar caso manual" mensaje={`Se incrementará el contador de casos activos de ${tecnicoPorAsignar.nombre}. Esta operación no crea una solicitud y no se revierte desde este módulo.`} textoConfirmar="Incrementar carga" cargando={guardando} alCancelar={() => setTecnicoPorAsignar(null)} alConfirmar={asignarCaso} />}
  </section>;
}
