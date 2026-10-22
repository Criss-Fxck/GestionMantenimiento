package service;

import domain.model.SolicitudMantenimiento;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class EmailService {
    private final String remitente;
    private final String claveAplicacion;

    public EmailService(
            @Value("${app.email.remitente}") String remitente,
            @Value("${app.email.clave}") String claveAplicacion) {
        this.remitente = remitente;
        this.claveAplicacion = claveAplicacion;
    }

    public void enviarCorreo(String emailDestino, String asunto, String cuerpoEmail) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(remitente, claveAplicacion);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(remitente));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailDestino));
            message.setSubject(asunto, "UTF-8");
            message.setText(cuerpoEmail, "UTF-8");
            Transport.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException("No fue posible enviar el correo", e);
        }
    }

    public void notificarSolicitudCreada(SolicitudMantenimiento solicitud) {
        String cuerpo = "Se ha creado la solicitud #" + solicitud.getIdSolicitud()
                + " para el equipo " + solicitud.getEquipo().getNombre()
                + ". Falla reportada: " + solicitud.getDescripcionFalla();

        enviarCorreo(
                solicitud.getSolicitante().getEmail(),
                "Notificacion Ticket #" + solicitud.getIdSolicitud(),
                cuerpo
        );
    }
}
