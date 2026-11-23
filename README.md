# Gestión de Mantenimiento

Aplicación académica para registrar equipos, gestionar solicitudes de mantenimiento, asignar técnicos, programar revisiones preventivas y enviar reportes por correo.

## Componentes

| Componente | Tecnología | Ubicación |
| --- | --- | --- |
| Backend REST | Java 21, Spring Boot 3.2.3, Maven | `src/Main/java` |
| Frontend | React y Vite | `frontend-app/` |
| Base de datos | MySQL en Aiven | Se inicializa y actualiza con Flyway |
| Correo | API HTTP de Brevo | Configuración por variables de entorno |

El frontend y el backend son proyectos separados y se comunican por HTTP. La base de datos y el servicio de correo no guardan sus credenciales en el repositorio.

## Requisitos

- JDK 21.
- Maven 3.9 o posterior (o el wrapper de Maven si se agrega al proyecto).
- Node.js y npm, compatibles con las versiones declaradas en `frontend-app/package-lock.json`.
- Una base MySQL accesible desde el backend. El proyecto está configurado para Aiven y exige SSL en la conexión de producción.
- Una cuenta de Brevo y un remitente verificado, si se van a probar las notificaciones.
- Docker Desktop para ejecutar las pruebas de integración que usan Testcontainers.

## Variables de entorno

Configura las variables en la ejecución del backend de IntelliJ IDEA o en el panel de variables del servicio de despliegue. No publiques los valores, no los añadas al README ni los guardes en Git.

### Backend

| Variable | Obligatoria | Uso / formato |
| --- | --- | --- |
| `SPRING_DATASOURCE_URL` | Sí | URL JDBC MySQL de Aiven: `jdbc:mysql://<host>:<puerto>/<base>?ssl-mode=REQUIRED`. Usa el host, puerto y nombre de base que muestra Aiven. |
| `SPRING_DATASOURCE_USERNAME` | Sí | Usuario de MySQL asignado por Aiven. |
| `SPRING_DATASOURCE_PASSWORD` | Sí | Contraseña de MySQL asignada por Aiven. |
| `BREVO_API_KEY` | Sí para correo | Clave de API transaccional de Brevo. Mantén este valor privado. |
| `BREVO_SENDER_EMAIL` | Sí para correo | Dirección del remitente verificado en Brevo. |
| `BREVO_SENDER_NAME` | No | Nombre visible del remitente. Por defecto: `Gestión de Mantenimiento`. |
| `BREVO_API_URL` | No | URL de envío de Brevo. Por defecto: `https://api.brevo.com/v3/smtp/email`. |
| `CORS_ALLOWED_ORIGINS` | No | Orígenes web permitidos, separados por coma. Por defecto local: `http://localhost:5173`. En despliegue, configura el dominio HTTPS del frontend. |
| `PORT` | No | Puerto HTTP. Por defecto local: `8080`; Render define el puerto del servicio. |

Para obtener la URL JDBC, toma el host, puerto, base de datos, usuario y contraseña del panel de Aiven. La URL debe comenzar con `jdbc:mysql://` y llevar `?ssl-mode=REQUIRED` para la conexión TLS de Aiven. No copies aquí la URI completa con credenciales.

### Frontend

| Variable | Obligatoria | Uso |
| --- | --- | --- |
| `VITE_API_BASE_URL` | No | URL raíz del backend, sin `/` final. Por defecto local: `http://localhost:8080`. En producción usa la URL HTTPS pública del backend. |

Para desarrollo local, el archivo de ejemplo está en `frontend-app/.env.example`. Si necesitas cambiar la URL, crea `frontend-app/.env.local` a partir del ejemplo. Los archivos `.env.local` están ignorados por Git. Vite incorpora `VITE_API_BASE_URL` durante la compilación; si cambia en producción, vuelve a compilar y desplegar el frontend.

## Ejecución local

### 1. Configurar e iniciar el backend

1. Abre el repositorio raíz en IntelliJ IDEA.
2. Configura JDK 21 como SDK del proyecto y como JDK de ejecución.
3. En **Run/Debug Configurations**, selecciona la clase principal `mantenimiento.main`.
4. En **Environment variables**, configura las variables del backend de la tabla anterior. Para trabajar sin correo, las variables `BREVO_*` solo son necesarias al usar una función que envía mensajes.
5. Ejecuta `mantenimiento.main`. Flyway crea/aplica las migraciones al iniciar Spring Boot y la API queda disponible en `http://localhost:8080` (salvo que definas `PORT`).

El directorio Maven de fuentes es intencionalmente `src/Main/java` (con `M` mayúscula); no lo renombres. Hibernate está configurado para validar el esquema, mientras Flyway administra las migraciones.

### 2. Iniciar el frontend

Desde una terminal en la raíz del repositorio:

```powershell
cd frontend-app
npm ci
npm run dev
```

Abre `http://localhost:5173`. El frontend usa `http://localhost:8080` por defecto y envía solicitudes a la API Spring Boot.

### 3. Comprobar la conexión

Con ambos procesos iniciados, prueba `http://localhost:8080/api/equipos` o usa el módulo **Equipos** del frontend. La base inicial incluye los equipos `EQ-100` y `EQ-200`, y los técnicos `T01` y `T02`.

## Esquema y migraciones

Flyway mantiene los scripts en `src/Main/resources/db/migration/` y los aplica automáticamente al arrancar el backend:

| Migración | Contenido |
| --- | --- |
| `V1__crear_tablas.sql` | Crea equipos, técnicos, solicitantes, solicitudes y revisiones preventivas; define llaves foráneas, índices y restricciones. La secuencia de solicitudes inicia en 1001. |
| `V2__datos_iniciales.sql` | Inserta los equipos `EQ-100` y `EQ-200` y los técnicos `T01` y `T02`. |
| `V3__agregar_marca_modelo_a_equipos.sql` | Añade marca y modelo a los equipos existentes. |

No ejecutes los scripts de nuevo manualmente después de que Flyway los haya aplicado. Para ver la base en Aiven, usa su panel SQL o un cliente MySQL con la conexión SSL configurada; la base está en la nube, no dentro de la carpeta del proyecto.

## Funciones y reglas de negocio

- Un equipo nuevo se registra con código, marca, modelo y ubicación, y comienza como `ACTIVO` (operativo). El código duplicado se rechaza.
- Una solicitud requiere un equipo activo y una descripción. El backend asigna al técnico disponible con menor carga.
- Un técnico admite hasta cinco casos activos. El sexto intento se rechaza con conflicto HTTP 409.
- El solicitante se registra o reutiliza por su ID. El correo de asignación forma parte del flujo de creación; si falla, el backend no confirma la solicitud.
- Para cerrar una solicitud se requieren diagnóstico y solución. El correo de cierre se envía antes de guardar el cierre; se libera la carga del técnico al completarse.
- El motor de revisiones se ejecuta cada 10 segundos. Al alcanzar la fecha de aviso, envía una alerta y genera una solicitud automática. Reprogramar una revisión reinicia el indicador de alerta.
- El reporte semanal y el módulo de correo de prueba usan Brevo.
- La API no implementa autenticación ni roles.

## API disponible

| Método | Ruta | Función |
| --- | --- | --- |
| `GET` | `/api/equipos` | Lista equipos. |
| `POST` | `/api/equipos` | Registra equipo (`idEquipo`, `marca`, `modelo`, `ubicacion`). |
| `POST` | `/api/equipos/{id}/fuera-de-servicio` | Marca un equipo fuera de servicio. |
| `GET` | `/api/solicitudes` | Lista solicitudes. |
| `POST` | `/api/solicitudes` | Registra y asigna solicitud (`equipoId`, datos del solicitante y `descripcion`). |
| `POST` | `/api/solicitudes/{id}/cerrar` | Cierra con `diagnostico` y `solucion`. |
| `GET` | `/api/tecnicos/carga` | Lista técnicos y ocupación. |
| `POST` | `/api/tecnicos` | Registra un técnico. |
| `POST` | `/api/tecnicos/{id}/asignar` | Incrementa manualmente la carga del técnico. |
| `GET` | `/api/revisiones` | Lista revisiones preventivas. |
| `POST` | `/api/revisiones` | Programa una revisión. |
| `PUT` | `/api/revisiones/{id}/reprogramar` | Cambia la fecha y reinicia la alerta. |
| `POST` | `/api/reportes/tecnicos/semanal/enviar` | Genera y envía el reporte semanal. |
| `POST` | `/api/email/enviar` | Envía un correo de prueba. |

Los errores de negocio se devuelven como JSON con `error` y `mensaje`, usando los códigos HTTP correspondientes (400, 404 o 409).

## Pruebas y compilación

Desde la raíz del repositorio, con JDK 21:

```powershell
mvn test
```

Las pruebas de integración usan Testcontainers y requieren que Docker esté disponible. Para compilar el frontend:

```powershell
cd frontend-app
npm ci
npm run build
```

## Despliegue acordado en Render

El frontend estático y el backend son servicios independientes; Aiven sigue alojando MySQL.

### Backend

1. Crea un **Web Service** en Render conectado a este repositorio.
2. Selecciona el entorno **Docker** y usa el `Dockerfile` de la raíz como archivo de construcción.
3. En **Environment**, configura las variables del backend de la tabla anterior: conexión Aiven, Brevo y CORS.
4. `CORS_ALLOWED_ORIGINS` debe contener el origen HTTPS exacto del frontend publicado.
5. Render proporciona `PORT`; Spring Boot lo usa automáticamente. Copia la URL HTTPS del backend.

### Frontend estático

1. Crea un **Static Site** en Render desde el mismo repositorio.
2. Configura **Root Directory** como `frontend-app`.
3. Usa **Build Command** `npm ci && npm run build` y **Publish Directory** `dist`.
4. Define `VITE_API_BASE_URL` con la URL HTTPS pública del backend y despliega el sitio.
5. Si cambias el dominio del frontend, actualiza `CORS_ALLOWED_ORIGINS` en el backend y vuelve a desplegarlo.

No se necesita publicar un archivo `render.yaml`: ambos servicios se pueden configurar desde el panel de Render. El backend requiere acceso de red a Aiven y a la API HTTPS de Brevo.

### Pendiente para completar y verificar el despliegue

El despliegue en Render todavía debe realizarse desde las cuentas del proyecto. La compilación local y las pruebas automatizadas no sustituyen la verificación de los servicios publicados. Sigue esta lista:

- [ ] Crear en Render el **Web Service** del backend usando el `Dockerfile` de la raíz.
- [ ] Configurar en Render las variables de conexión a Aiven (`SPRING_DATASOURCE_*`) y correo (`BREVO_*`). No pegarlas en Git ni en el frontend.
- [ ] Crear en Render el **Static Site** con raíz `frontend-app`, comando `npm ci && npm run build` y directorio publicado `dist`.
- [ ] Configurar `VITE_API_BASE_URL` con la URL HTTPS del backend y `CORS_ALLOWED_ORIGINS` con el origen HTTPS exacto del frontend; volver a desplegar ambos servicios si se cambia una URL.
- [ ] Revisar los logs del backend y confirmar que inicia, conecta con Aiven y Flyway aplica o valida las migraciones sin errores.
- [ ] Abrir el frontend publicado y confirmar que las peticiones a la API no fallan por URL o CORS.
- [ ] Hacer una prueba controlada de punta a punta en producción: registrar un equipo, crear una solicitud y comprobar la asignación y el correo, cerrar la solicitud y confirmar que se libera la carga del técnico.
- [ ] Probar también las funciones publicadas de técnicos, revisiones preventivas, reprogramación, reporte semanal y correo de prueba; revisar las respuestas de error para datos inválidos y códigos duplicados.
- [ ] Confirmar el comportamiento del proceso programado de revisiones con el servicio backend publicado y activo, y verificar la recepción del correo de alerta.

Las pruebas de producción pueden crear datos persistentes en Aiven y enviar correos reales. Usa datos identificables de prueba y destinatarios controlados; al terminar, revisa la base y conserva o elimina esos datos manualmente según lo que necesite el equipo. La API no ofrece endpoints de borrado.

## Seguridad de configuración

- No subas `.env`, `.env.local`, contraseñas JDBC, claves de Brevo ni credenciales de Aiven.
- `frontend-app/.env.example` contiene solo una URL local de ejemplo. Nunca pongas secretos en variables `VITE_*`: Vite las incorpora en los archivos públicos del frontend.
- Si una credencial se expone accidentalmente, revócala y genera otra en el proveedor correspondiente.
