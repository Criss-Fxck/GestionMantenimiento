import { apiRequest } from './apiClient.js';

export function enviarReporteSemanal(emailCoordinador) {
  return apiRequest('/api/reportes/tecnicos/semanal/enviar', {
    method: 'POST',
    body: JSON.stringify({ emailCoordinador }),
  });
}
