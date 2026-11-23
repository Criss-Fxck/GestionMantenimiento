import { apiRequest } from './apiClient.js';

export function obtenerCargaTecnicos() {
  return apiRequest('/api/tecnicos/carga');
}

export function crearTecnico(tecnico) {
  return apiRequest('/api/tecnicos', { method: 'POST', body: JSON.stringify(tecnico) });
}

export function asignarCasoManual(id) {
  return apiRequest(`/api/tecnicos/${encodeURIComponent(id)}/asignar`, { method: 'POST' });
}
