import { useCallback, useEffect, useMemo, useState } from 'react';
import ConfirmacionModal from '../components/ConfirmacionModal.jsx';
import EstadoCarga from '../components/EstadoCarga.jsx';
import TablaDatos from '../components/TablaDatos.jsx';
import Toast from '../components/Toast.jsx';
import { crearEquipo, marcarFueraDeServicio, obtenerEquipos } from '../api/equiposApi.js';

const formularioInicial = { idEquipo: '', marca: '', modelo: '', ubicacion: '' };

function etiquetaEstado(estado) {
  if (estado === 'ACTIVO') return { texto: 'Operativo', estilo: 'status-operativo' };
  if (estado?.toLowerCase() === 'fuera de servicio') return { texto: 'Fuera de servicio', estilo: 'status-fuera' };
  if (estado === 'CRÍTICO') return { texto: 'Crítico', estilo: 'status-critico' };
  return { texto: estado || '—', estilo: 'status-neutral' };
}

export default function EquiposPage() {
  const [equipos, setEquipos] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [errorCarga, setErrorCarga] = useState('');
  const [busqueda, setBusqueda] = useState('');
  const [filtroEstado, setFiltroEstado] = useState('todos');
  const [mostrarFormulario, setMostrarFormulario] = useState(false);
  const [formulario, setFormulario] = useState(formularioInicial);
  const [guardando, setGuardando] = useState(false);
  const [equipoPorInhabilitar, setEquipoPorInhabilitar] = useState(null);
  const [actualizandoEstado, setActualizandoEstado] = useState(false);
  const [aviso, setAviso] = useState(null);

  const cargarEquipos = useCallback(async () => {
    setCargando(true);
    setErrorCarga('');
    try {
      const respuesta = await obtenerEquipos();
      setEquipos(Array.isArray(respuesta) ? respuesta : []);
    } catch (error) {
      setErrorCarga(error.message);
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => { cargarEquipos(); }, [cargarEquipos]);

  const equiposFiltrados = useMemo(() => {
    const termino = busqueda.trim().toLocaleLowerCase('es-CO');
    return equipos.filter((equipo) => {
      const coincideTexto = !termino || [
        equipo.idEquipo, equipo.marca, equipo.modelo, equipo.nombre, equipo.ubicacion, equipo.estado,
      ].some((valor) => String(valor ?? '').toLocaleLowerCase('es-CO').includes(termino));
      const coincideEstado = filtroEstado === 'todos'
        || (filtroEstado === 'activo' && equipo.estado === 'ACTIVO')
        || (filtroEstado === 'critico' && equipo.estado === 'CRÍTICO')
        || (filtroEstado === 'fuera' && equipo.estado?.toLowerCase() === 'fuera de servicio');
      return coincideTexto && coincideEstado;
    });
  }, [equipos, busqueda, filtroEstado]);

  const columnas = useMemo(() => [
    { clave: 'idEquipo', titulo: 'Código' },
    { clave: 'marca', titulo: 'Marca', render: (equipo) => equipo.marca || '—' },
    { clave: 'modelo', titulo: 'Modelo', render: (equipo) => equipo.modelo || '—' },
    { clave: 'ubicacion', titulo: 'Ubicación' },
    {
      clave: 'estado',
      titulo: 'Estado',
      render: (equipo) => {
        const etiqueta = etiquetaEstado(equipo.estado);
        return <span className={`status-badge ${etiqueta.estilo}`}>{etiqueta.texto}</span>;
      },
    },
    {
      titulo: 'Acciones',
      render: (equipo) => (equipo.estado?.toLowerCase() === 'fuera de servicio' ? (
        <span className="action-completed">Sin acciones</span>
      ) : (
        <button className="btn btn-outline-danger btn-sm" type="button" onClick={() => setEquipoPorInhabilitar(equipo)}>
          <i className="bi bi-slash-circle me-1" aria-hidden="true" />Fuera de servicio
        </button>
      )),
    },
  ], []);

  function actualizarCampo(evento) {
    const { name, value } = evento.target;
    setFormulario((actual) => ({ ...actual, [name]: value }));
  }

  function cerrarFormulario() {
    if (guardando) return;
    setMostrarFormulario(false);
    setFormulario(formularioInicial);
  }

  async function enviarFormulario(evento) {
    evento.preventDefault();
    setGuardando(true);
    try {
      const creado = await crearEquipo({
        idEquipo: formulario.idEquipo.trim(),
        marca: formulario.marca.trim(),
        modelo: formulario.modelo.trim(),
        ubicacion: formulario.ubicacion.trim(),
      });
      setEquipos((actuales) => [...actuales, creado]);
      setMostrarFormulario(false);
      setFormulario(formularioInicial);
      setAviso({ tipo: 'exito', mensaje: `El equipo ${creado.idEquipo} quedó registrado como operativo.` });
    } catch (error) {
      setAviso({ tipo: 'error', mensaje: error.message });
    } finally {
      setGuardando(false);
    }
  }

  async function confirmarFueraDeServicio() {
    if (!equipoPorInhabilitar) return;
    setActualizandoEstado(true);
    try {
      const actualizado = await marcarFueraDeServicio(equipoPorInhabilitar.idEquipo);
      setEquipos((actuales) => actuales.map((equipo) => (
        equipo.idEquipo === actualizado.idEquipo ? actualizado : equipo
      )));
      setEquipoPorInhabilitar(null);
      setAviso({ tipo: 'exito', mensaje: `El equipo ${actualizado.idEquipo} quedó fuera de servicio.` });
    } catch (error) {
      setAviso({ tipo: 'error', mensaje: error.message });
    } finally {
      setActualizandoEstado(false);
    }
  }

  return (
    <section className="page-section">
      <Toast aviso={aviso} alCerrar={() => setAviso(null)} />
      <div className="page-heading">
        <div>
          <p className="eyebrow">INVENTARIO DE ACTIVOS</p>
          <h1>Equipos</h1>
          <p className="page-description">Administra los equipos y consulta su estado operativo.</p>
        </div>
        <button className="btn btn-primary primary-action" type="button" onClick={() => setMostrarFormulario(true)}>
          <i className="bi bi-plus-lg me-2" aria-hidden="true" />Registrar equipo
        </button>
      </div>

      <div className="table-card">
        <div className="table-toolbar">
          <div className="search-control">
            <i className="bi bi-search" aria-hidden="true" />
            <input
              className="form-control"
              type="search"
              aria-label="Buscar equipos"
              placeholder="Buscar por código, marca o ubicación"
              value={busqueda}
              onChange={(evento) => setBusqueda(evento.target.value)}
            />
          </div>
          <label className="filter-control">
            <span>Estado</span>
            <select className="form-select" value={filtroEstado} onChange={(evento) => setFiltroEstado(evento.target.value)}>
              <option value="todos">Todos</option>
              <option value="activo">Operativo</option>
              <option value="critico">Crítico</option>
              <option value="fuera">Fuera de servicio</option>
            </select>
          </label>
          <span className="result-count">{equiposFiltrados.length} equipos</span>
        </div>

        {cargando ? (
          <EstadoCarga mensaje="Cargando equipos…" />
        ) : errorCarga ? (
          <div className="load-error" role="alert">
            <i className="bi bi-exclamation-circle" aria-hidden="true" />
            <span>{errorCarga}</span>
            <button className="btn btn-outline-primary btn-sm" type="button" onClick={cargarEquipos}>Reintentar</button>
          </div>
        ) : (
          <TablaDatos
            columnas={columnas}
            filas={equiposFiltrados}
            claveFila={(equipo) => equipo.idEquipo}
            textoVacio={equipos.length ? 'No se encontraron equipos con esos criterios.' : 'Todavía no hay equipos registrados.'}
          />
        )}
      </div>

      {mostrarFormulario && (
        <div className="modal-backdrop-custom" role="presentation" onMouseDown={(evento) => {
          if (evento.target === evento.currentTarget) cerrarFormulario();
        }}>
          <section className="form-modal" role="dialog" aria-modal="true" aria-labelledby="equipo-modal-title">
            <div className="form-modal-heading">
              <div>
                <p className="eyebrow">NUEVO ACTIVO</p>
                <h2 id="equipo-modal-title">Registrar equipo</h2>
              </div>
              <button className="icon-button" type="button" aria-label="Cerrar formulario" disabled={guardando} onClick={cerrarFormulario}>
                <i className="bi bi-x-lg" aria-hidden="true" />
              </button>
            </div>
            <form onSubmit={enviarFormulario}>
              <div className="form-field">
                <label htmlFor="equipo-codigo">Código <span aria-hidden="true">*</span></label>
                <input id="equipo-codigo" className="form-control" name="idEquipo" maxLength="30" required value={formulario.idEquipo} onChange={actualizarCampo} autoFocus />
              </div>
              <div className="form-row">
                <div className="form-field">
                  <label htmlFor="equipo-marca">Marca <span aria-hidden="true">*</span></label>
                  <input id="equipo-marca" className="form-control" name="marca" maxLength="120" required value={formulario.marca} onChange={actualizarCampo} />
                </div>
                <div className="form-field">
                  <label htmlFor="equipo-modelo">Modelo <span aria-hidden="true">*</span></label>
                  <input id="equipo-modelo" className="form-control" name="modelo" maxLength="120" required value={formulario.modelo} onChange={actualizarCampo} />
                </div>
              </div>
              <div className="form-field">
                <label htmlFor="equipo-ubicacion">Ubicación física <span aria-hidden="true">*</span></label>
                <input id="equipo-ubicacion" className="form-control" name="ubicacion" maxLength="160" required value={formulario.ubicacion} onChange={actualizarCampo} />
              </div>
              <div className="initial-state-note">
                <i className="bi bi-check-circle" aria-hidden="true" />
                El equipo se registrará automáticamente como operativo.
              </div>
              <div className="modal-actions">
                <button className="btn btn-light" type="button" disabled={guardando} onClick={cerrarFormulario}>Cancelar</button>
                <button className="btn btn-primary" type="submit" disabled={guardando}>
                  {guardando && <span className="spinner-border spinner-border-sm me-2" aria-hidden="true" />}
                  Guardar equipo
                </button>
              </div>
            </form>
          </section>
        </div>
      )}

      {equipoPorInhabilitar && (
        <ConfirmacionModal
          titulo="Marcar equipo fuera de servicio"
          mensaje={`¿Confirmas que el equipo ${equipoPorInhabilitar.idEquipo} quede fuera de servicio? No se podrán crear solicitudes para este equipo.`}
          textoConfirmar="Confirmar cambio"
          cargando={actualizandoEstado}
          alCancelar={() => setEquipoPorInhabilitar(null)}
          alConfirmar={confirmarFueraDeServicio}
        />
      )}
    </section>
  );
}
