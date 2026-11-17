import { apiRequest } from './apiClient.js';

export function enviarCorreo(datos) {
  return apiRequest('/api/email/enviar', { method: 'POST', body: JSON.stringify(datos) });
}
