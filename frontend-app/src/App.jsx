import { useState } from 'react';
import Sidebar from './components/Sidebar.jsx';
import CorreoPage from './pages/CorreoPage.jsx';
import EquiposPage from './pages/EquiposPage.jsx';
import ReportesPage from './pages/ReportesPage.jsx';
import RevisionesPage from './pages/RevisionesPage.jsx';
import SolicitudesPage from './pages/SolicitudesPage.jsx';
import TecnicosPage from './pages/TecnicosPage.jsx';

const secciones = [
  { id: 'equipos', label: 'Equipos', icon: 'bi-pc-display-horizontal', disponible: true },
  { id: 'solicitudes', label: 'Solicitudes', icon: 'bi-clipboard-check', disponible: true },
  { id: 'tecnicos', label: 'Técnicos y carga', icon: 'bi-people', disponible: true },
  { id: 'revisiones', label: 'Revisiones preventivas', icon: 'bi-calendar2-check', disponible: true },
  { id: 'reportes', label: 'Reportes', icon: 'bi-bar-chart', disponible: true },
  { id: 'correo', label: 'Correo de prueba', icon: 'bi-envelope', disponible: true },
];

export default function App() {
  const [menuAbierto, setMenuAbierto] = useState(false);
  const [seccionActiva, setSeccionActiva] = useState('equipos');
  const paginas = {
    equipos: <EquiposPage />,
    solicitudes: <SolicitudesPage />,
    tecnicos: <TecnicosPage />,
    revisiones: <RevisionesPage />,
    reportes: <ReportesPage />,
    correo: <CorreoPage />,
  };

  return (
    <div className="app-shell">
      <Sidebar
        secciones={secciones}
        activa={seccionActiva}
        abierta={menuAbierto}
        alCerrar={() => setMenuAbierto(false)}
        alSeleccionar={(id) => { setSeccionActiva(id); setMenuAbierto(false); }}
      />
      {menuAbierto && (
        <button
          className="sidebar-backdrop"
          type="button"
          aria-label="Cerrar menú"
          onClick={() => setMenuAbierto(false)}
        />
      )}
      <div className="app-content">
        <header className="topbar">
          <button
            className="btn btn-light sidebar-toggle"
            type="button"
            aria-label="Abrir menú"
            onClick={() => setMenuAbierto(true)}
          >
            <i className="bi bi-list" aria-hidden="true" />
          </button>
          <span className="topbar-caption">Sistema de gestión de activos</span>
          <span className="environment-label">Desarrollo</span>
        </header>
        <main className="main-content">
          {paginas[seccionActiva]}
        </main>
      </div>
    </div>
  );
}
