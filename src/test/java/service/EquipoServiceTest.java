package service;

import domain.exeptions.RecursoDuplicadoException;
import domain.exeptions.RecursoNoEncontradoException;
import domain.model.Equipo;
import domain.repository.EquipoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EquipoServiceTest {
    @Mock private EquipoRepository equipoRepository;
    private EquipoService equipoService;

    @BeforeEach
    void setUp() {
        equipoService = new EquipoService(equipoRepository);
        lenient().when(equipoRepository.guardar(any())).thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    @Test
    void consultaEquipoPorIdEnElRepositorio() {
        Equipo esperado = new Equipo("EQ-01", "Marca", "Modelo", "Taller");
        when(equipoRepository.buscarPorId("EQ-01")).thenReturn(Optional.of(esperado));

        assertEquals(esperado, equipoService.obtenerPorId("EQ-01"));
    }

    @Test
    void rechazaUnCodigoDuplicado() {
        when(equipoRepository.buscarPorId("EQ-01")).thenReturn(Optional.of(new Equipo("EQ-01", "Equipo", "Taller")));

        assertThrows(RecursoDuplicadoException.class,
                () -> equipoService.registrarEquipo("EQ-01", "Marca", "Modelo", "Taller"));
    }

    @Test
    void actualizaDatosSinCambiarElEstadoDelEquipo() {
        Equipo equipo = new Equipo("EQ-01", "Marca anterior", "Modelo anterior", "Taller");
        equipo.marcarFueraDeServicio();
        when(equipoRepository.buscarPorId("EQ-01")).thenReturn(Optional.of(equipo));

        Equipo resultado = equipoService.actualizarEquipo("EQ-01", "Marca nueva", "Modelo nuevo", "Piso 2");

        assertEquals("Marca nueva", resultado.getMarca());
        assertEquals("Modelo nuevo", resultado.getModelo());
        assertEquals("Piso 2", resultado.getUbicacion());
        assertEquals("Fuera de servicio", resultado.getEstado());
        verify(equipoRepository).guardar(equipo);
    }

    @Test
    void informaSiNoExisteElEquipoSolicitado() {
        when(equipoRepository.buscarPorId("EQ-INEXISTENTE")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> equipoService.obtenerPorId("EQ-INEXISTENTE"));
    }
}
