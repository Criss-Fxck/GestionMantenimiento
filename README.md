# Gestión de mantenimiento

API Spring Boot para registrar equipos y solicitudes de mantenimiento y enviar notificaciones por correo mediante Gmail SMTP.
## Solución de Problemas (Troubleshooting)


### Error de Byte Buddy / Mockito al ejecutar pruebas unitarias
**Problema:** Al correr las pruebas con JUnit o Pitest, la consola arroja un error indicando que la versión de Java no es compatible, por ejemplo:
`java.lang.IllegalArgumentException: Java 27 (71) is not supported by the current version of Byte Buddy...`

**Causa:** IntelliJ IDEA está intentando compilar y ejecutar el proyecto utilizando un JDK más reciente o experimental (como Java 27) en lugar del entorno estable configurado en el `pom.xml` (Java 21). Esto rompe la compatibilidad con Mockito.

**Solución (Project Structure):**
1. En IntelliJ IDEA, abre **File > Project Structure...** (o presiona `Ctrl + Alt + Shift + S`).
2. En el menú izquierdo, selecciona **Project**.
3. En el campo **SDK**, despliega el menú y selecciona una versión de **Java 21** (si no tienes una, selecciona *Download JDK...* y descarga la versión 21).
4. En el campo **Language level**, selecciona `21 - No new language features`.
5. Haz clic en **Apply** y luego en **OK**.
6. *(Opcional)* Ve a **File > Settings > Build, Execution, Deployment > Build Tools > Maven > Runner** y asegúrate de que **JRE** esté configurado en `Use Project JDK` o directamente en `21`.
7. Vuelve a ejecutar las pruebas.
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

## Cierre formal de solicitudes y asignación automática de técnicos

### Asignación al crear una solicitud

Al recibir `POST /api/solicitudes`, el backend selecciona automáticamente al técnico que tenga menos casos activos entre los que tengan menos de cinco. Si varios técnicos comparten la menor carga, se asigna cualquiera de ellos según el orden entregado por el repositorio. El caso queda asociado a la solicitud y aumenta en uno la carga activa del técnico. El estado inicial de la solicitud pasa a `ASIGNADO`.

La asignación automática no requiere enviar el identificador del técnico en el JSON. La respuesta de creación incluye la solicitud y el técnico asignado.

Si no hay técnicos registrados o todos tienen cinco casos activos, la solicitud no se crea y la API responde `409 Conflict` con un mensaje que indica que no hay técnicos disponibles. La notificación por correo sigue siendo parte de la creación: si el envío falla, la solicitud no se agrega a la lista ni se asigna el caso.

### Cerrar una solicitud

El endpoint para registrar el trabajo y cerrar una solicitud es:

`POST /api/solicitudes/{id}/cerrar`

Ejemplo de cuerpo JSON:

```json
{
  "diagnostico": "El rodamiento del motor estaba desgastado.",
  "solucion": "Se reemplazó el rodamiento y se verificó el funcionamiento."
}
```

Ambos campos son obligatorios y deben contener texto distinto de espacios. La API elimina espacios sobrantes al inicio y al final antes de guardar los valores. Si falta el diagnóstico o la solución, o está vacío, responde `400 Bad Request` con un objeto JSON que contiene `error` y `mensaje`; no cierra la solicitud.

Cuando los datos son válidos, la solicitud pasa al estado `Cerrado`, conserva el diagnóstico y la solución, y registra `fechaHoraCierre` con la fecha y hora del servidor. La respuesta `200 OK` contiene la solicitud actualizada, incluido el técnico asignado y el historial de cierre. La fecha se serializa como fecha y hora ISO local (sin información de zona horaria).

Al cerrar, el contador de casos activos del técnico asignado disminuye en uno. Así, ese técnico puede recibir nuevas solicitudes siempre que su carga vuelva a estar por debajo del máximo de cinco.

### Respuestas para situaciones no válidas

- Solicitud inexistente: `404 Not Found`, con `error` y `mensaje`.
- Solicitud ya cerrada: `409 Conflict`; se conserva el cierre original.
- Solicitud sin diagnóstico o solución válida: `400 Bad Request`; el estado permanece sin cerrar.
- Todos los técnicos ocupados o sin técnicos registrados al crear una solicitud: `409 Conflict`; no se crea la solicitud.
- Error de envío del correo al crear la solicitud: la operación falla y la solicitud no se registra.

### Persistencia y alcance

Los técnicos y las solicitudes se mantienen en memoria, como el resto de los datos de esta aplicación; se pierden al reiniciar el proceso. La asignación automática se aplica a solicitudes nuevas creadas mediante `POST /api/solicitudes`. El endpoint previo `POST /api/tecnicos/{id}/asignar` continúa incrementando únicamente el contador manual del técnico y no queda asociado a una solicitud específica. Este backend no incluye interfaz gráfica: el cliente que lo consuma debe llamar al endpoint de cierre y mostrar el `mensaje` de error cuando la API rechace la petición.

### Notificaciones por cambio de estado

Cada transición de estado de una solicitud notifica por correo al email guardado para su solicitante. Al crear la solicitud se envía el estado `ASIGNADO`, el equipo y el técnico asignado. Al cerrarla se envía el estado `Cerrado`, el equipo, el diagnóstico y la solución. Si el envío de cualquiera de estas notificaciones falla, la transición correspondiente no se aplica: al crear, la solicitud no se registra; al cerrar, conserva su estado y datos anteriores. Los intentos rechazados por datos inválidos, solicitud inexistente o solicitud ya cerrada no generan notificación porque no cambian el estado.

### Secuencia Postman para comprobar el flujo

Antes de iniciar, configura `MAIL_USERNAME` y `MAIL_PASSWORD` en el entorno del backend, inicia la aplicación y confirma que el correo del solicitante de prueba puede recibir mensajes. En Postman ejecuta estas peticiones en el orden indicado. Para volver a empezar con los técnicos de prueba y el contador de solicitudes inicial, reinicia la aplicación; los datos son en memoria.

1. **Consultar técnicos y carga inicial**

   `GET http://localhost:8080/api/tecnicos/carga`

   Deben aparecer los técnicos de prueba (por ejemplo `T01` y `T02`) con cero casos activos.

2. **Crear una solicitud y verificar asignación y notificación**

   `POST http://localhost:8080/api/solicitudes`

   Header: `Content-Type: application/json`

   ```json
   {
     "equipoId": "EQ-101",
     "equipoNombre": "Laptop Lenovo ThinkPad",
     "equipoUbicacion": "Oficina 302",
     "solicitanteId": "USR-001",
     "solicitanteNombre": "Williams Balan",
     "departamento": "Sistemas",
     "email": "tu-correo-de-prueba@example.com",
     "descripcion": "Mantenimiento preventivo"
   }
   ```

   Espera `201 Created`; la respuesta contiene el `idSolicitud`, `estado: ASIGNADO` y `tecnicoAsignado`. Comprueba que llegó el correo de asignación. Con el estado inicial de una ejecución limpia, el identificador será `1001`; usa el identificador real devuelto para los pasos siguientes.

3. **Confirmar que la solicitud aparece en el historial**

   `GET http://localhost:8080/api/solicitudes`

   La lista debe contener la solicitud creada y el técnico asignado.

4. **Intentar cerrar sin diagnóstico**

   `POST http://localhost:8080/api/solicitudes/1001/cerrar`

   ```json
   {
     "diagnostico": "",
     "solucion": "Se reemplazó el componente."
   }
   ```

   Espera `400 Bad Request`, con `error` y `mensaje`. La solicitud debe continuar en `ASIGNADO` y no debe enviarse un correo de cierre. Si el identificador devuelto en el paso 2 no es `1001`, reemplázalo en esta y las siguientes peticiones.

5. **Cerrar correctamente y verificar notificación**

   `POST http://localhost:8080/api/solicitudes/1001/cerrar`

   ```json
   {
     "diagnostico": "El rodamiento del motor estaba desgastado.",
     "solucion": "Se reemplazó el rodamiento y se verificó el funcionamiento."
   }
   ```

   Espera `200 OK`. La respuesta debe incluir `estado: Cerrado`, los campos `diagnostico` y `solucion`, y `fechaHoraCierre`. Comprueba que llegó el correo de cierre al mismo solicitante.

6. **Verificar historial y liberación de carga**

   Ejecuta de nuevo `GET http://localhost:8080/api/solicitudes` y `GET http://localhost:8080/api/tecnicos/carga`. La solicitud debe seguir cerrada con su historial y la carga activa del técnico asignado debe haber disminuido en uno.

7. **Comprobar que un cierre repetido se rechaza**

   Repite la petición de cierre del paso 5 para el mismo identificador. Espera `409 Conflict`; el cierre original debe conservarse y no debe enviarse otra notificación de cambio de estado.

Si el correo falla por configuración o destinatario, la API no confirma una transición exitosa. Revisa las variables `MAIL_USERNAME` y `MAIL_PASSWORD` y los registros de la aplicación antes de repetir el paso.
