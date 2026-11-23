import { apiRequest } from './apiClient.js';

export function obtenerEquipos() {
  return apiRequest('/api/equipos');
}

export function crearEquipo(equipo) {
  return apiRequest('/api/equipos', {
    method: 'POST',
    body: JSON.stringify(equipo),
  });
}

export function marcarFueraDeServicio(idEquipo) {
  return apiRequest(`/api/equipos/${encodeURIComponent(idEquipo)}/fuera-de-servicio`, {
    method: 'POST',
  });
}
