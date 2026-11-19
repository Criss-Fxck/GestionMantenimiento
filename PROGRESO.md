# PROGRESO — GestionMantenimiento

> Archivo de continuidad. Léelo primero al iniciar cualquier sesión y continúa desde "Siguiente paso exacto".

## Estado actual

- **Fase:** 1 — Auditoría (solo lectura) ✅ terminada el 2026-11-19.
- **Módulo actual:** ninguno (todavía no hay plan aprobado).
- **Siguiente paso exacto:** esperar las respuestas del usuario a las preguntas abiertas (sección "Preguntas abiertas") y luego redactar el plan de la Fase 2.

## Checklist de módulos

(Se definirá en la Fase 2. Propuesta preliminar.)

| Módulo | Estado |
| --- | --- |
| 0. Entorno de trabajo (JDK, Maven, Node, variables de entorno) | ⬜ pendiente |
| 1. Conexión BD + CORS + manejo global de errores | ⬜ pendiente |
| 2. Técnicos | ⬜ pendiente |
| 3. Equipos | ⬜ pendiente |
| 4. Solicitudes | ⬜ pendiente |
| 5. Revisiones preventivas / motor de alertas | ⬜ pendiente |
| 6. Reportes y correo de prueba | ⬜ pendiente |
| 7. Regresión completa + GUIA_PRUEBAS.md | ⬜ pendiente |

## Decisiones aprobadas por el usuario

| Fecha | Decisión |
| --- | --- |
| 2026-11-19 | Todo el trabajo (conversación, código, comentarios, nombres, documentación) se hace en español. |

## Preguntas abiertas

Ver la lista numerada entregada al final de la Fase 1 en la conversación (se copiarán aquí las respuestas).

## Hallazgos clave de la Fase 1

- El proyecto **no es un repositorio Git** (no hay `.git`).
- El frontend ya usa `fetch` real contra la API (no se encontraron datos ficticios ni `localStorage`).
- El backend ya persiste en MySQL (Aiven) con JPA + Flyway. Quedan restos de listas en memoria dentro de los servicios (solo se usan cuando los repositorios son `null`, es decir, en pruebas unitarias) y `TecnicoRepositoryInMemory` (perfil `memoria`, inactivo por defecto).
- Herramientas del equipo: el `java` del sistema es solo JRE (sin `javac`). JDK completo en `~/.jdks/ms-21.0.13.0.2`. Maven solo existe dentro de IntelliJ. No hay Node.js, npm ni Docker instalados. Hay cliente MySQL/MariaDB en `/opt/lampp/bin/mysql`.
- Variables de entorno de BD y Brevo **no** están definidas en la terminal (solo en IntelliJ, si acaso).

## Archivos creados/modificados

| Módulo | Archivos |
| --- | --- |
| Fase 1 | `PROGRESO.md` (creado) |

## Resultado de las últimas pruebas

- Comando: `JAVA_HOME=~/.jdks/ms-21.0.13.0.2 /opt/idea-IU-262.11879.100/plugins/maven-plugin/lib/maven3/bin/mvn -B test` (ejecutado sobre una copia en `/tmp/gm-auditoria` para no tocar el proyecto).
- Resultado (2026-11-19): **BUILD SUCCESS** — 20 pruebas, 0 fallos, 0 errores, **2 omitidas** (`PersistenciaMySQLTest` y `FlujosApiMySQLTest` se saltan porque no hay Docker para Testcontainers).
- Las 18 pruebas unitarias que pasaron (Email, Reporte, Revision, Solicitud, Tecnico, Verificacion) usan Mockito o listas en memoria; ninguna toca la BD real.

## Comandos para retomar

```bash
# Backend (requiere variables SPRING_DATASOURCE_*, BREVO_*)
cd /home/chris/Descargas/Universidad/Proyectos/POo/GestionMantenimiento-despliegue
export JAVA_HOME=~/.jdks/ms-21.0.13.0.2
/opt/idea-IU-262.11879.100/plugins/maven-plugin/lib/maven3/bin/mvn -B test
/opt/idea-IU-262.11879.100/plugins/maven-plugin/lib/maven3/bin/mvn spring-boot:run

# Frontend (requiere instalar Node.js primero)
cd frontend-app && npm ci && npm run dev
```
