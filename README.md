# Gestión de mantenimiento

API Spring Boot para registrar equipos y solicitudes de mantenimiento y enviar notificaciones por correo mediante Gmail SMTP.

## Configurar el correo

El backend lee estas variables de entorno al iniciar:

- `MAIL_USERNAME`: dirección Gmail remitente.
- `MAIL_PASSWORD`: contraseña de aplicación de Google (no la contraseña normal de la cuenta).

En IntelliJ IDEA, abre **Run > Edit Configurations...**, selecciona la configuración de `mantenimiento.main` y agrega ambas variables en **Environment variables** con el formato `MAIL_USERNAME=correo@gmail.com;MAIL_PASSWORD=contraseña_de_aplicación`. Guarda la configuración y reinicia la aplicación.

También puedes iniciarla desde PowerShell configurando las variables solo para esa ventana:

```powershell
$env:MAIL_USERNAME = "correo@gmail.com"
$env:MAIL_PASSWORD = "contraseña_de_aplicación"
mvn spring-boot:run
```

No guardes esos valores en `application.properties`, archivos de Postman, ni en el repositorio. Si la credencial anterior llegó a estar en un commit o se compartió, revócala en Google y genera una nueva; borrar el valor del archivo actual no lo elimina del historial de Git.

## Usar con Postman

Importa `postman/collections/GestionMantenimiento.postman_collection.json` y `postman/environments/Local.postman_environment.json`. Selecciona el entorno **Local** y ejecuta las peticiones. El correo y su contraseña se configuran en el entorno de ejecución del backend; no son variables de Postman.

La API escucha por defecto en `http://localhost:8080`:

- `GET /api/equipos`
- `POST /api/equipos`
- `GET /api/solicitudes`
- `POST /api/solicitudes` (crea la solicitud y envía su notificación)
- `POST /api/email/enviar`

Si no se configuran las variables de correo, la aplicación puede iniciar, pero los endpoints que envían correo devolverán un error en lugar de informar un envío exitoso.
### Cuerpos para las peticiones POST

En Postman, selecciona **Body > raw > JSON** y copia el cuerpo correspondiente. Sustituye `destinatario@ejemplo.com` por el correo que recibira la notificacion.

#### POST `{{baseUrl}}/api/equipos`

```json
{
  "idEquipo": "EQ-101",
  "nombre": "Laptop Lenovo ThinkPad",
  "ubicacion": "Oficina 302"
}
```

#### POST `{{baseUrl}}/api/solicitudes`

```json
{
  "equipoId": "EQ-101",
  "equipoNombre": "Laptop Lenovo ThinkPad",
  "equipoUbicacion": "Oficina 302",
  "solicitanteId": "USR-001",
  "solicitanteNombre": "Williams Balan",
  "departamento": "Sistemas",
  "email": "destinatario@ejemplo.com",
  "descripcion": "Mantenimiento preventivo"
}
```

Esta peticion crea la solicitud y envia una notificacion al correo indicado en `email`.

#### POST `{{baseUrl}}/api/email/enviar`

```json
{
  "destinatario": "destinatario@ejemplo.com",
  "asunto": "Prueba desde Postman",
  "cuerpo": "Correo de prueba de la API de mantenimiento."
}
```

Las peticiones GET (`/api/equipos` y `/api/solicitudes`) no requieren body.

 SPRINT 0 

 cambios por matias: 
 
### Validación de Asignación de Casos a Técnicos (Sobrecarga Laboral) 

El sistema incluye una regla de negocio que limita a 5 el número de casos activos por técnico. Para validar esta funcionalidad a través de la API (estando en la rama `main` y con la aplicación en ejecución), sigue estos pasos desde Postman:

1. **Crear un Técnico de Prueba:**
   - **Método:** `POST`
   - **Endpoint:** `http://localhost:8080/api/tecnicos`
   - **Body (raw/JSON):**
     ```json
     {
       "idUsuario": "T03",
       "nombre": "Carlos Restrepo",
       "departamento": "Mantenimiento",
       "especialidad": "Sistemas"
     }
     ```
2. **Asignar Casos (Acumular Carga):**
   - **Método:** `POST`
   - **Endpoint:** `http://localhost:8080/api/tecnicos/T03/asignar`
   - **Acción:** Ejecuta esta petición 5 veces. Observarás que el sistema retorna un código `200 OK` en cada intento, indicando que la asignación fue exitosa.
3. **Validar la Regla de Sobrecarga (El Bloqueo):**
   - **Acción:** Ejecuta la misma petición de asignación por sexta vez.
   - **Resultado Esperado:** El sistema debe retornar un código **HTTP 409 Conflict** con un cuerpo JSON indicando el error de sobrecarga laboral (Ej. `"mensaje": "El técnico Carlos Restrepo ya tiene el máximo de 5 casos asignados."`).
4. **Verificar el Estado Final (Carga y Disponibilidad):**
   - **Método:** `GET`
   - **Endpoint:** `http://localhost:8080/api/tecnicos/carga`
   - **Resultado Esperado:** En la lista, el técnico `T03` debe reflejar exactamente `5` casos activos, un estado de `OCUPADO_AL_LIMITE` y un `porcentajeOcupacion` del `100%`.
