export default function Sidebar({ secciones, activa, abierta, alCerrar, alSeleccionar }) {
  return (
    <aside className={`sidebar ${abierta ? 'sidebar-open' : ''}`} aria-label="Navegación principal">
      <div className="sidebar-brand">
        <span className="brand-kicker">MANTENIMIENTO</span>
        <span className="brand-name">Gestión de<br />Mantenimiento</span>
      </div>
      <nav className="sidebar-nav">
        <span className="nav-section-label">MÓDULOS</span>
        {secciones.map((seccion) => (
          <button
            key={seccion.id}
            className={`sidebar-link ${activa === seccion.id ? 'sidebar-link-active' : ''}`}
            type="button"
            disabled={!seccion.disponible}
            aria-current={activa === seccion.id ? 'page' : undefined}
            title={!seccion.disponible ? 'Este módulo se habilitará en su turno de implementación.' : undefined}
            onClick={() => { alSeleccionar(seccion.id); alCerrar(); }}
          >
            <i className={`bi ${seccion.icon}`} aria-hidden="true" />
            <span>{seccion.label}</span>
            {!seccion.disponible && <span className="nav-coming-soon">Próximo</span>}
          </button>
        ))}
      </nav>
      <div className="sidebar-footer">Sistema académico de mantenimiento</div>
    </aside>
  );
}
