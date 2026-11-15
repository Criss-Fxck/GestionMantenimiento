package domain.model;

import domain.exeptions.SobrecargaLaboralException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TecnicoTest {

    private Tecnico tecnico;

    // @BeforeEach ejecuta este bloque antes de cada @Test para iniciar siempre con un tÃ©cnico limpio.
    @BeforeEach
    void setUp() {
        tecnico = new Tecnico("T01", "Matias", "Mantenimiento", "Sistemas");
    }

    // @Test indica que este bloque es una prueba unitaria
    @Test
    void deberiaEstarLibreAlCrearse() {
        assertEquals(0, tecnico.getCasosActivos(), "El tÃ©cnico debe iniciar con 0 casos");
        assertEquals(EstadoDisponibilidad.LIBRE, tecnico.getDisponibilidad(), "El estado inicial debe ser LIBRE");
    }

    @Test
    void deberiaEstarParcialmenteOcupadoAlAsignarCasos() {
        tecnico.asignarNuevoCaso();

        assertEquals(1, tecnico.getCasosActivos());
        assertEquals(EstadoDisponibilidad.PARCIALMENTE_OCUPADO, tecnico.getDisponibilidad(), "El estado debe cambiar al asignar un caso");
    }

    @Test
    void deberiaLanzarExcepcionAlSuperarLimiteDeCasos() {
        // Llenamos la capacidad permitida de 5 casos
        for (int i = 0; i < 5; i++) {
            tecnico.asignarNuevoCaso();
        }

        assertEquals(EstadoDisponibilidad.OCUPADO_AL_LIMITE, tecnico.getDisponibilidad());

        // assertThrows atrapa la excepcion para asegurar que el sistema bloquea el sexto caso
        assertThrows(SobrecargaLaboralException.class, () -> {
            tecnico.asignarNuevoCaso();
        }, "Se esperaba una SobrecargaLaboralException al asignar el sexto caso");
    }
}
