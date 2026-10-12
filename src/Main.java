import domain.model.*;
import infrastructure.email.EmailService;

public class Main {

    public static void probarUsuario(Usuario usuario) {
        usuario.mostrarInformacion();
    }

    public static void main(String[] args) {

        // Instanciación de objetos
        UsuarioSolicitante solicitante1 = new UsuarioSolicitante("US-01", "Ana Gómez", "Contabilidad");
        Tecnico tecnico1 = new Tecnico("TEC-01", "Carlos Mendoza", "Sistemas");
        Equipo equipo1 = new Equipo("EQ-100", "Impresora Industrial", "Piso 2");

        // Polimorfismo
        probarUsuario(solicitante1);
        probarUsuario(tecnico1);

        System.out.println("");

        // Mostrar equipo
        equipo1.mostrarInformacion();

        System.out.println("");

        // Creación y gestión de solicitud
        SolicitudMantenimiento solicitud1 = new SolicitudMantenimiento(1001, equipo1, solicitante1, "Falla en rodillos de impresión");

        // Mostrar detalle inicial
        solicitud1.mostrarDetalle();

        // Asignar técnico
        solicitud1.asignarTecnico(tecnico1);
        System.out.println("-> Se ha asignado técnico a la solicitud #1001.");
        solicitud1.mostrarDetalle();

        // Ejecutar notificación de correo
        // Construir el cuerpo del mensaje
        String cuerpoNotificacion = "Se ha creado la solicitud #" + solicitud1.getIdSolicitud() +
                " para el equipo " + solicitud1.getEquipo().getNombre() +
                ". Falla reportada: " + solicitud1.getDescripcionFalla();

// Ejecutar notificación de correo
        EmailService.enviarCorreo(
                "nicolas.munoz.lopera@gmail.com",
                "Notificación Ticket #" + solicitud1.getIdSolicitud(),
                cuerpoNotificacion
        );
    }
}