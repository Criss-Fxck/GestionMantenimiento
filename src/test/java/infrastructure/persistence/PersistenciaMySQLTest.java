package infrastructure.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import domain.model.Equipo;
import domain.model.RevisionPreventiva;
import domain.model.SolicitudMantenimiento;
import domain.model.Tecnico;
import domain.model.UsuarioSolicitante;
import domain.repository.RevisionPreventivaRepository;
import domain.repository.SolicitanteRepository;
import domain.repository.SolicitudRepository;
import domain.repository.TecnicoRepository;
import service.EquipoService;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import mantenimiento.main;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = main.class)
@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PersistenciaMySQLTest {

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.36");

    @Autowired
    private EquipoService equipoService;

    @Autowired
    private TecnicoRepository tecnicoRepository;

    @Autowired
    private SolicitanteRepository solicitanteRepository;

    @Autowired
    private SolicitudRepository solicitudRepository;

    @Autowired
    private RevisionPreventivaRepository revisionRepository;

    @DynamicPropertySource
    static void configurarDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }

    @Test
    void aplicaMigracionesYGuardaEquiposEnMySQL() {
        assertEquals(2, equipoService.obtenerTodos().size());

        Equipo equipo = equipoService.registrarEquipo(
                "EQ-TEST-DB", "Marca de prueba", "Modelo de prueba", "Laboratorio");

        assertTrue(equipoService.buscarPorId("EQ-TEST-DB").isPresent());
        assertEquals("Marca de prueba", equipoService.buscarPorId("EQ-TEST-DB").orElseThrow().getMarca());
        assertEquals("Modelo de prueba", equipoService.buscarPorId("EQ-TEST-DB").orElseThrow().getModelo());

        Tecnico tecnico = tecnicoRepository.buscarPorId("T01").orElseThrow();
        UsuarioSolicitante solicitante = solicitanteRepository.guardar(
                new UsuarioSolicitante("USR-TEST-DB", "Usuario de prueba", "Pruebas", "test@example.com"));
        tecnico.asignarNuevoCaso();
        tecnicoRepository.guardar(tecnico);

        SolicitudMantenimiento solicitud = solicitudRepository.guardar(
                new SolicitudMantenimiento(equipo, solicitante, "Prueba de persistencia"));
        solicitud.asignarTecnico(tecnico);
        solicitudRepository.guardar(solicitud);

        assertEquals(1001, solicitud.getIdSolicitud());
        assertEquals("T01", solicitudRepository.buscarPorId(1001).orElseThrow()
                .getTecnicoAsignado().getIdUsuario());

        RevisionPreventiva revision = revisionRepository.guardar(new RevisionPreventiva(
                "REV-TEST-DB", equipo, LocalDate.now(), 3, "supervisor@example.com"));
        assertNotNull(revisionRepository.buscarPorId(revision.getIdRevision()).orElseThrow());
    }
}
