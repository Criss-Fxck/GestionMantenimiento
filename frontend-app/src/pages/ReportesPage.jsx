import { useState } from 'react';
import { enviarReporteSemanal } from '../api/reportesApi.js';
import EstadoCarga from '../components/EstadoCarga.jsx';
import TablaDatos from '../components/TablaDatos.jsx';
import Toast from '../components/Toast.jsx';

const fechaLocal = (valor) => valor ? new Intl.DateTimeFormat('es-CO', { dateStyle: 'medium', timeZone: 'UTC' }).format(new Date(`${valor}T00:00:00Z`)) : '—';

export default function ReportesPage() {
  const [correo, setCorreo] = useState('');
  const [reporte, setReporte] = useState(null);
  const [cargando, setCargando] = useState(false);
  const [aviso, setAviso] = useState(null);

  async function generar(evento) {
    evento.preventDefault(); setCargando(true); setAviso(null);
    try {
      const resultado = await enviarReporteSemanal(correo.trim());
      setReporte(resultado);
      setAviso({ tipo: 'exito', mensaje: `El reporte semanal fue enviado a ${correo.trim()}.` });
    } catch (error) { setAviso({ tipo: 'error', mensaje: error.message }); }
    finally { setCargando(false); }
  }

  const columnas = [
    { clave: 'nombre', titulo: 'Técnico' },
    { clave: 'departamento', titulo: 'Departamento' },
    { clave: 'especialidad', titulo: 'Especialidad' },
    { clave: 'casosActivos', titulo: 'Casos activos' },
    { clave: 'porcentajeOcupacion', titulo: 'Ocupación', valorOrden: (item) => item.casosActivos },
    { clave: 'totalSolicitudes', titulo: 'Solicitudes semana' },
    { clave: 'solicitudesReportadas', titulo: 'Reportadas' },
    { clave: 'solicitudesAsignadas', titulo: 'Asignadas' },
    { clave: 'solicitudesCerradas', titulo: 'Cerradas' },
  ];

  return <section className="page-section">
    <Toast aviso={aviso} alCerrar={() => setAviso(null)} />
    <div className="page-heading"><div><p className="eyebrow">SEGUIMIENTO OPERATIVO</p><h1>Reportes</h1><p className="page-description">Consulta el resumen semanal por técnico y envíalo al coordinador.</p></div></div>
    <div className="table-card report-request-card"><div className="report-intro"><span className="report-icon"><i className="bi bi-bar-chart-line" aria-hidden="true" /></span><div><h2>Reporte semanal de técnicos</h2><p>Incluye carga actual y solicitudes registradas de lunes a domingo.</p></div></div>
      <form className="report-form" onSubmit={generar}><div className="form-field"><label htmlFor="reporte-email">Correo del coordinador <span>*</span></label><input id="reporte-email" className="form-control" type="email" maxLength="254" required placeholder="coordinador@empresa.com" value={correo} onChange={(event) => setCorreo(event.target.value)} /></div><button className="btn btn-primary primary-action" type="submit" disabled={cargando}>{cargando ? <><span className="spinner-border spinner-border-sm me-2" aria-hidden="true" />Generando y enviando</> : <><i className="bi bi-send me-2" aria-hidden="true" />Generar y enviar</>}</button></form>
      <p className="report-note"><i className="bi bi-info-circle me-2" aria-hidden="true" />El backend genera el resumen y lo envía por correo; la tabla se muestra cuando el envío termina correctamente.</p>
    </div>
    {cargando && <div className="table-card mt-4"><EstadoCarga mensaje="Generando el reporte y enviándolo…" /></div>}
    {reporte && !cargando && <div className="report-results"><div className="report-results-heading"><div><h2>Resumen semanal enviado</h2><p>Semana del {fechaLocal(reporte.inicioSemana)} al {fechaLocal(reporte.finSemana)}</p></div><span className="status-badge status-operativo"><i className="bi bi-check-circle me-1" aria-hidden="true" />Enviado</span></div><div className="table-card"><TablaDatos columnas={columnas} filas={reporte.tecnicos || []} claveFila={(item) => item.idUsuario} textoVacio="No hay técnicos para incluir en el reporte." /></div></div>}
  </section>;
}
