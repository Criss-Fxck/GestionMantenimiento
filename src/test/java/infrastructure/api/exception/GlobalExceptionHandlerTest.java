package infrastructure.api.exception;

import domain.exeptions.ServicioCorreoException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void traduceFalloDeCorreoA502ConFormatoDeErrorApi() {
        var respuesta = handler.handleServicioCorreo(
                new ServicioCorreoException("No se pudo enviar el correo mediante Brevo."));

        assertEquals(HttpStatus.BAD_GATEWAY, respuesta.getStatusCode());
        assertEquals("Error de correo", respuesta.getBody().get("error"));
        assertTrue(respuesta.getBody().get("mensaje").startsWith("No se pudo enviar el correo"));
    }
}
