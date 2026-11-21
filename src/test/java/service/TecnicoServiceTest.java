package service;

import domain.exeptions.RecursoDuplicadoException;
import domain.exeptions.RecursoNoEncontradoException;
import domain.model.EstadoDisponibilidad;
import domain.model.Tecnico;
import domain.repository.TecnicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.dto.TecnicoCreacionDTO;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TecnicoServiceTest {
    @Mock private TecnicoRepository tecnicoRepository;
    private TecnicoService tecnicoService;

    @BeforeEach
    void setUp() {
        tecnicoService = new TecnicoService(tecnicoRepository);
    }

    @Test
    void incluyeDepartamentoYEspecialidadEnElResumenDeCarga() {
        Tecnico tecnico = new Tecnico("T-01", "Ana", "Operaciones", "Eléctrica");
        when(tecnicoRepository.obtenerTodos()).thenReturn(List.of(tecnico));

        var fila = tecnicoService.consultarCargaTecnicos().getFirst();

        assertEquals("Operaciones", fila.getDepartamento());
        assertEquals("Eléctrica", fila.getEspecialidad());
        assertEquals(EstadoDisponibilidad.LIBRE, fila.getEstado());
    }

    @Test
    void actualizaDatosDelTecnicoSinCambiarSuCarga() {
        Tecnico tecnico = new Tecnico("T-01", "Ana", "Operaciones", "Eléctrica");
        tecnico.asignarNuevoCaso();
        when(tecnicoRepository.buscarPorId("T-01")).thenReturn(Optional.of(tecnico));
        TecnicoCreacionDTO cambios = new TecnicoCreacionDTO();
        cambios.setNombre("Ana María");
        cambios.setDepartamento("Mantenimiento");
        cambios.setEspecialidad("Electrónica");

        Tecnico actualizado = tecnicoService.actualizarTecnico("T-01", cambios);

        assertEquals("Ana María", actualizado.getNombre());
        assertEquals("Mantenimiento", actualizado.getDepartamento());
        assertEquals("Electrónica", actualizado.getEspecialidad());
        assertEquals(1, actualizado.getCasosActivos());
        verify(tecnicoRepository).guardar(tecnico);
    }

    @Test
    void rechazaRegistrarUnTecnicoDuplicado() {
        when(tecnicoRepository.buscarPorId("T-01")).thenReturn(Optional.of(new Tecnico("T-01", "Ana", "Mantenimiento", "Eléctrica")));
        TecnicoCreacionDTO nuevo = dto("T-01", "Ana", "Mantenimiento", "Eléctrica");

        assertThrows(RecursoDuplicadoException.class, () -> tecnicoService.registrarNuevoTecnico(nuevo));
    }

    @Test
    void respondeComoNoEncontradoAlConsultarIdInexistente() {
        when(tecnicoRepository.buscarPorId("T-404")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> tecnicoService.obtenerPorId("T-404"));
    }

    private TecnicoCreacionDTO dto(String id, String nombre, String departamento, String especialidad) {
        TecnicoCreacionDTO dto = new TecnicoCreacionDTO();
        dto.setIdUsuario(id);
        dto.setNombre(nombre);
        dto.setDepartamento(departamento);
        dto.setEspecialidad(especialidad);
        return dto;
    }
}
