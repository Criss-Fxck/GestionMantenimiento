# Manual de ejecución y validación

## Requisitos locales

- JDK 21 completo (`java -version` y `javac -version`).
- Maven 3.8 o posterior disponible como `mvn` o desde IntelliJ IDEA.
- Node.js 20.19+ o 22+ y npm.
- Archivo `.env` local en la raíz con las variables que aparecen en `.env.example`. Nunca compartas ni agregues `.env` al repositorio.
- Para las pruebas que envían correo, definir `CORREO_PRUEBA` en el entorno del cliente HTTP con una dirección controlada por el equipo. Las operaciones que envían correo se indican abajo.

## Iniciar los servicios en Linux

En una terminal, desde la raíz:

```bash
./iniciar-backend.sh
```

El backend queda listo cuando Spring registra `Started GestionMantenimientoApplication` (el nombre de la clase puede variar) y el proceso se mantiene activo. En los logs de inicio también debe aparecer el pool Hikari iniciado; un error de conexión o validación de esquema de Flyway/Hibernate indica que Aiven no quedó listo. El script lee `.env` sin imprimirlo.

En otra terminal:

```bash
cd frontend-app
npm ci
npm run dev
```

Vite muestra `Local: http://localhost:5173/`. Abre esa dirección. El frontend usa `http://localhost:8080` como API por defecto; si se cambia, define `VITE_API_BASE_URL` antes de iniciar Vite.

Las pruebas unitarias (excluye las suites de integración que requieren Docker):

```bash
mvn test
```

## APIs con curl

Ejecuta los ejemplos en orden. Los IDs `PRUEBA-` permiten reconocer los datos; las solicitudes tienen un ID numérico que se obtiene de la respuesta del POST. Los comandos de equipos, técnicos y revisiones no envían correo. Una revisión se programa para 2099 para que el motor no genere alertas durante la prueba.

```bash
BASE=http://localhost:8080

# Equipos: POST 201, lista, detalle y PUT 200
curl -i -X POST "$BASE/api/equipos" -H 'Content-Type: application/json' \
  -d '{"idEquipo":"PRUEBA-EQ01","marca":"PRUEBA Marca","modelo":"Equipo de ensayo","ubicacion":"Laboratorio"}'
curl -i "$BASE/api/equipos"
curl -i "$BASE/api/equipos/PRUEBA-EQ01"
curl -i -X PUT "$BASE/api/equipos/PRUEBA-EQ01" -H 'Content-Type: application/json' \
  -d '{"marca":"PRUEBA Marca actualizada","modelo":"Equipo de ensayo","ubicacion":"Laboratorio actualizado"}'

# Técnicos: POST 201, carga, detalle y PUT 200
curl -i -X POST "$BASE/api/tecnicos" -H 'Content-Type: application/json' \
  -d '{"idUsuario":"PRUEBA-T01","nombre":"Técnico de prueba","departamento":"PRUEBA Mantenimiento","especialidad":"PRUEBA General"}'
curl -i "$BASE/api/tecnicos/carga"
curl -i "$BASE/api/tecnicos/PRUEBA-T01"
curl -i -X PUT "$BASE/api/tecnicos/PRUEBA-T01" -H 'Content-Type: application/json' \
  -d '{"idUsuario":"PRUEBA-T01","nombre":"Técnico editado","departamento":"PRUEBA Mantenimiento","especialidad":"PRUEBA Electromecánica"}'

# Revisión preventiva: POST 201, lista, detalle, PUT y reprogramar
curl -i -X POST "$BASE/api/revisiones" -H 'Content-Type: application/json' \
  -d '{"idRevision":"PRUEBA-REV01","equipoId":"PRUEBA-EQ01","fechaProgramada":"2099-12-31","diasAvisoPrevio":3,"emailSupervisor":"'"$CORREO_PRUEBA"'"}'
curl -i "$BASE/api/revisiones"
curl -i "$BASE/api/revisiones/PRUEBA-REV01"
curl -i -X PUT "$BASE/api/revisiones/PRUEBA-REV01" -H 'Content-Type: application/json' \
  -d '{"equipoId":"PRUEBA-EQ01","fechaProgramada":"2099-12-30","diasAvisoPrevio":5,"emailSupervisor":"'"$CORREO_PRUEBA"'"}'
curl -i -X PUT "$BASE/api/revisiones/PRUEBA-REV01/reprogramar" -H 'Content-Type: application/json' \
  -d '{"nuevaFecha":"2099-12-29"}'

# Errores esperados: 400, 404 y 409. El JSON devuelve {"error":"...","mensaje":"..."}
curl -i -X POST "$BASE/api/equipos" -H 'Content-Type: application/json' -d '{"idEquipo":""}'
curl -i "$BASE/api/equipos/PRUEBA-NO-EXISTE"
curl -i -X POST "$BASE/api/equipos" -H 'Content-Type: application/json' \
  -d '{"idEquipo":"PRUEBA-EQ01","marca":"PRUEBA Marca","modelo":"Duplicado","ubicacion":"Laboratorio"}'
```

Para revisar el resto de operaciones, usa `pruebas.http` desde IntelliJ IDEA/VS Code REST Client. Antes, configura `CORREO_PRUEBA` en el entorno del cliente HTTP para resolver `{{$dotenv CORREO_PRUEBA}}`.

### Solicitudes y correos

`POST /api/solicitudes`, `POST /api/solicitudes/{id}/cerrar`, `/api/email/enviar`, el envío de reportes y el botón “Generar y enviar” de Reportes producen correo real. Ejecútalos solo después de configurar `CORREO_PRUEBA` con el buzón controlado del equipo. El alta de una solicitud persiste datos, asigna técnico y envía notificación. Captura el `idSolicitud` de la respuesta y úsalo en `GET /api/solicitudes/{id}`. El error 502 de correo se valida de forma controlada con `mvn test` (`EmailServiceTest` y `GlobalExceptionHandlerTest`); no fuerces una credencial inválida en una llamada real.

La suite no asigna casos manualmente a técnicos para no dejar carga artificial persistida. El límite de cinco casos se valida en pruebas unitarias.

## Validación desde el navegador

1. En **Equipos**, registra `PRUEBA-EQ01`, marca `PRUEBA Marca`, modelo `Equipo de ensayo` y ubicación `Laboratorio`; verifica que aparece en la tabla. Usa **Ver** para consultar por ID y **Editar** para cambiar la ubicación.
2. En **Técnicos y carga**, registra `PRUEBA-T01`, con nombre, departamento y especialidad que comiencen por `PRUEBA-`. Comprueba que los tres campos aparecen en la tabla; usa **Ver** y **Editar**.
3. En **Revisiones preventivas**, programa `PRUEBA-REV01` para el equipo anterior con fecha `2099-12-31`, anticipación `3` días y `CORREO_PRUEBA` como correo del supervisor. Consulta y edita desde la tabla.
4. En **Solicitudes**, envía un reporte `PRUEBA-` solo con el buzón controlado ya configurado; verifica la fila, solicitante, equipo y técnico asignado. Esto envía correo real.
5. Abre DevTools con `F12` → **Red**. Las llamadas deben ir a `localhost:8080`, devolver códigos esperados y no mostrar errores CORS. Los fallos de API aparecen en pantalla con el campo `error` y el `mensaje`.
6. Recarga con `F5`: equipos, técnicos, revisiones y solicitudes deben seguir apareciendo porque se consultan desde la API. Reinicia el backend y vuelve a cargar; la persistencia se comprueba además consultando las tablas en Aiven.

## Limpieza manual en Aiven

Ejecuta estas sentencias manualmente en este orden, después de confirmar que los IDs corresponden a esta prueba. Solo eliminan solicitudes vinculadas al equipo y solicitante identificados y los registros `PRUEBA-` usados en esta suite:

```sql
DELETE FROM solicitudes
WHERE equipo_id = 'PRUEBA-EQ01'
  AND solicitante_id = 'PRUEBA-USR01'
  AND descripcion_falla LIKE 'PRUEBA-%';

DELETE FROM revisiones_preventivas
WHERE id_revision = 'PRUEBA-REV01';

DELETE FROM solicitantes
WHERE id_usuario = 'PRUEBA-USR01';

DELETE FROM tecnicos
WHERE id_usuario = 'PRUEBA-T01';

DELETE FROM equipos
WHERE id_equipo = 'PRUEBA-EQ01';
```

Elimina primero las solicitudes y revisiones porque tienen claves foráneas al equipo, solicitante o técnico. Si se usó un ID distinto para un registro de prueba, reemplaza ese ID exacto en la sentencia correspondiente.
