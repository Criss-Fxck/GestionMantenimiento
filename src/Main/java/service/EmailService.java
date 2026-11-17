package service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {
    private final HttpClient clienteHttp;
    private final ObjectMapper serializadorJson;
    private final String claveApi;
    private final String correoRemitente;
    private final String nombreRemitente;
    private final String urlApi;

    @Autowired
    public EmailService(
            @Value("${app.email.api-key}") String claveApi,
            @Value("${app.email.sender-email}") String correoRemitente,
            @Value("${app.email.sender-name:Gestión de Mantenimiento}") String nombreRemitente,
            ObjectMapper serializadorJson,
            @Value("${app.email.api-url:https://api.brevo.com/v3/smtp/email}") String urlApi) {
        this(HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .build(),
                serializadorJson,
                claveApi,
                correoRemitente,
                nombreRemitente,
                urlApi);
    }

    EmailService(
            HttpClient clienteHttp,
            ObjectMapper serializadorJson,
            String claveApi,
            String correoRemitente,
            String nombreRemitente,
            String urlApi) {
        this.clienteHttp = clienteHttp;
        this.serializadorJson = serializadorJson;
        this.claveApi = claveApi;
        this.correoRemitente = correoRemitente;
        this.nombreRemitente = nombreRemitente;
        this.urlApi = urlApi;
    }

    public void enviarCorreo(String emailDestino, String asunto, String cuerpoEmail) {
        if (claveApi == null || claveApi.isBlank()
                || correoRemitente == null || correoRemitente.isBlank()) {
            throw new IllegalStateException(
                    "Configura las variables BREVO_API_KEY y BREVO_SENDER_EMAIL para enviar correos.");
        }

        try {
            String cuerpoSolicitud = serializadorJson.writeValueAsString(Map.of(
                    "sender", Map.of("name", nombreRemitente, "email", correoRemitente),
                    "to", List.of(Map.of("email", emailDestino)),
                    "subject", asunto,
                    "textContent", cuerpoEmail));

            HttpRequest solicitudHttp = HttpRequest.newBuilder(URI.create(urlApi))
                    .timeout(Duration.ofSeconds(20))
                    .header("accept", "application/json")
                    .header("api-key", claveApi)
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(cuerpoSolicitud, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> respuesta = clienteHttp.send(
                    solicitudHttp,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (respuesta.statusCode() < 200 || respuesta.statusCode() >= 300) {
                throw new IllegalStateException(
                        "Brevo rechazó el correo (HTTP " + respuesta.statusCode() + ").");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Se interrumpió el envío del correo.", e);
        } catch (IOException | IllegalArgumentException e) {
            throw new IllegalStateException(
                    "No fue posible enviar el correo mediante Brevo.", e);
        }
    }
}
