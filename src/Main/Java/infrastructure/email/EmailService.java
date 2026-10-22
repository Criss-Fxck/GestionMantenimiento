package infrastructure.email;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {

    // Configuración del remitente (tu cuenta de Gmail)
    private static final String REMITENTE = System.getenv("EMAIL_REMITENTE");
    private static final String CLAVE_APLICACION = System.getenv("EMAIL_CLAVE_APP"); // Las 16 letras de Google

    public static void enviarCorreo(String emailDestino, String asunto, String cuerpoEmail) {
        // 1. Configuración de propiedades SMTP de Gmail
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // 2. Autenticación
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(REMITENTE, CLAVE_APLICACION);
            }
        });

        try {
            // 3. Crear el mensaje
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(REMITENTE));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailDestino));
            message.setSubject(asunto);
            message.setText(cuerpoEmail);

            // 4. Enviar
            Transport.send(message);
            System.out.println("✅ Correo enviado exitosamente a: " + emailDestino);

        } catch (MessagingException e) {
            System.err.println("❌ Error al enviar el correo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}