package infrastructure.email;

import domain.model.SolicitudMantenimiento;

public class EmailService {

    public static void enviarTicketPorCorreo(String emailDestino, SolicitudMantenimiento solicitud) {
        System.out.println("\n==================================================");
        System.out.println("📩 [API REST / EMAIL SERVICE] Generando notificación");
        System.out.println("==================================================");

        String asunto = "Ticket #" + solicitud.getIdSolicitud() + " - Estado: " + solicitud.getEstado();

        String cuerpoEmail = "Hola " + solicitud.getSolicitante().getNombre() + ",\n\n"
                + "Tu solicitud de mantenimiento ha sido procesada correctamente.\n\n"
                + "RESUMEN DEL TICKET:\n"
                + "- Número de Ticket: #" + solicitud.getIdSolicitud() + "\n"
                + "- Estado Actual: " + solicitud.getEstado() + "\n"
                + "- domain.model.Equipo: " + solicitud.getEquipo().getNombre() + " (" + solicitud.getEquipo().getUbicacion() + ")\n"
                + "- Descripción de la Falla: " + solicitud.getDescripcionFalla() + "\n"
                + "- Técnico Asignado: " + (solicitud.getTecnicoAsignado() != null ? solicitud.getTecnicoAsignado().getNombre() : "Pendiente") + "\n\n"
                + "Atentamente,\nSistema de Mantenimiento.";

        System.out.println("Para: " + emailDestino);
        System.out.println("Asunto: " + asunto);
        System.out.println("\nCuerpo del mensaje:\n" + cuerpoEmail);
        System.out.println("--------------------------------------------------");
        System.out.println("✅ [STATUS 200 OK] Correo enviado exitosamente a " + emailDestino);
        System.out.println("==================================================\n");
    }
}