package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private HttpClient clienteHttp;

    @Mock
    private HttpResponse<String> respuestaHttp;

    private EmailService emailService;

    @BeforeEach
    void configurarServicio() {
        emailService = new EmailService(
                clienteHttp,
                new ObjectMapper(),
                "api-key-de-prueba",
                "notificaciones@example.com",
                "Gestión de Mantenimiento",
                "https://api.brevo.com/v3/smtp/email");
    }

    @Test
    void enviarCorreoEnvíaSolicitudHttpConLaClaveApi() throws Exception {
        when(respuestaHttp.statusCode()).thenReturn(201);
        when(clienteHttp.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(respuestaHttp);

        assertDoesNotThrow(() -> emailService.enviarCorreo(
                "destinatario@example.com", "Asunto", "Contenido"));

        ArgumentCaptor<HttpRequest> solicitud = ArgumentCaptor.forClass(HttpRequest.class);
        verify(clienteHttp).send(solicitud.capture(), any(HttpResponse.BodyHandler.class));
        assertEquals("POST", solicitud.getValue().method());
        assertEquals("https://api.brevo.com/v3/smtp/email", solicitud.getValue().uri().toString());
        assertEquals("api-key-de-prueba", solicitud.getValue().headers()
                .firstValue("api-key").orElseThrow());
    }

    @Test
    void enviarCorreoFallaSiBrevoRechazaLaSolicitud() throws Exception {
        when(respuestaHttp.statusCode()).thenReturn(401);
        when(clienteHttp.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(respuestaHttp);

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> emailService.enviarCorreo("destinatario@example.com", "Asunto", "Contenido"));

        assertEquals("Brevo rechazó el correo (HTTP 401).", error.getMessage());
    }

    @Test
    void enviarCorreoFallaSiFaltaLaClaveApi() {
        EmailService servicioSinClave = new EmailService(
                clienteHttp,
                new ObjectMapper(),
                " ",
                "notificaciones@example.com",
                "Gestión de Mantenimiento",
                "https://api.brevo.com/v3/smtp/email");

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> servicioSinClave.enviarCorreo("destinatario@example.com", "Asunto", "Contenido"));

        assertEquals(
                "Configura las variables BREVO_API_KEY y BREVO_SENDER_EMAIL para enviar correos.",
                error.getMessage());
    }
}
