import domain.model.*;
import infrastructure.api.EquipoAPI;
import infrastructure.api.SolicitudAPI;
import service.EquipoService;
import service.SolicitudService;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Instanciación de Servicios y APIs
        EquipoService equipoService = new EquipoService();
        SolicitudService solicitudService = new SolicitudService();

        EquipoAPI equipoAPI = new EquipoAPI(equipoService);
        SolicitudAPI solicitudAPI = new SolicitudAPI(solicitudService);

        Scanner scanner = new Scanner(System.in);
        boolean salir = false;

        System.out.println("=============================================");
        System.out.println("   SISTEMA DE GESTIÓN DE MANTENIMIENTO");
        System.out.println("=============================================");

        while (!salir) {
            System.out.println("\n--- MENÚ DE CONSOLA (CONSUMO DE APIs) ---");
            System.out.println("1. Listar Equipos (API Equipos)");
            System.out.println("2. Registrar Nuevo Equipo (API Equipos)");
            System.out.println("3. Listar Solicitudes (API Solicitudes)");
            System.out.println("4. Crear Nueva Solicitud y Enviar Correo (API Solicitudes)");
            System.out.println("5. Salir");
            System.out.print("Seleccione una opción: ");

            int opcion = scanner.nextInt();
            scanner.nextLine(); // Limpiar el buffer de entrada

            switch (opcion) {
                case 1:
                    System.out.println("\n--- LISTA DE EQUIPOS ---");
                    for (Equipo eq : equipoAPI.listarEquipos()) {
                        eq.mostrarInformacion();
                        System.out.println("--------------------");
                    }
                    break;

                case 2:
                    System.out.print("ID del Equipo: ");
                    String idEq = scanner.nextLine();
                    System.out.print("Nombre del Equipo: ");
                    String nombreEq = scanner.nextLine();
                    System.out.print("Ubicación: ");
                    String ubicacion = scanner.nextLine();

                    equipoAPI.crearEquipo(idEq, nombreEq, ubicacion);
                    System.out.println("✅ Equipo registrado exitosamente a través de la API.");
                    break;

                case 3:
                    System.out.println("\n--- LISTA DE SOLICITUDES ---");
                    for (SolicitudMantenimiento sol : solicitudAPI.listarSolicitudes()) {
                        sol.mostrarDetalle();
                    }
                    break;

                case 4:
                    System.out.print("ID Usuario Solicitante: ");
                    String idUsr = scanner.nextLine();
                    System.out.print("Nombre Solicitante: ");
                    String nomUsr = scanner.nextLine();
                    System.out.print("Departamento: ");
                    String depto = scanner.nextLine();

                    UsuarioSolicitante solicitante = new UsuarioSolicitante(idUsr, nomUsr, depto);

                    System.out.print("ID Equipo a reparar: ");
                    String idEqSol = scanner.nextLine();
                    System.out.print("Nombre Equipo: ");
                    String nomEqSol = scanner.nextLine();
                    System.out.print("Ubicación Equipo: ");
                    String ubiEqSol = scanner.nextLine();

                    Equipo equipoSol = new Equipo(idEqSol, nomEqSol, ubiEqSol);

                    System.out.print("Descripción de la falla: ");
                    String desc = scanner.nextLine();

                    System.out.println("\nProcesando solicitud y enviando notificación...");
                    SolicitudMantenimiento solCreada = solicitudAPI.registrarSolicitud(equipoSol, solicitante, desc);
                    System.out.println("✅ Solicitud #" + solCreada.getIdSolicitud() + " registrada y correo enviado.");
                    break;

                case 5:
                    salir = true;
                    System.out.println("Saliendo del programa...");
                    break;

                default:
                    System.out.println("Opción no válida.");
            }
        }

        scanner.close();
    }
}