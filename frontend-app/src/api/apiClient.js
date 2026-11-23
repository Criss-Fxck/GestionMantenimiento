const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/$/, '');

export async function apiRequest(path, options = {}) {
  const headers = new Headers(options.headers || {});
  if (options.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  let response;
  try {
    response = await fetch(`${apiBaseUrl}${path}`, { ...options, headers });
  } catch {
    throw new Error('No se pudo conectar con el servidor. Verifica que la API esté disponible.');
  }

  const contenido = await response.text();
  let datos = null;
  if (contenido) {
    try {
      datos = JSON.parse(contenido);
    } catch {
      datos = { mensaje: contenido };
    }
  }

  if (!response.ok) {
    const error = new Error(datos?.mensaje || `La solicitud falló (HTTP ${response.status}).`);
    error.status = response.status;
    throw error;
  }

  return datos;
}
