package service;

import domain.model.Tecnico;
import domain.repository.TecnicoRepository;
import org.springframework.stereotype.Service;
import service.dto.TecnicoCargaDTO;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TecnicoService {

    private final TecnicoRepository tecnicoRepository;

    // Inyección de dependencias
    public TecnicoService(TecnicoRepository tecnicoRepository) {
        this.tecnicoRepository = tecnicoRepository;
    }

    public List<TecnicoCargaDTO> consultarCargaTecnicos() {
        return tecnicoRepository.obtenerTodos().stream()
                .map(t -> new TecnicoCargaDTO(
                        t.getIdUsuario(),
                        t.getNombre(),
                        t.getCasosActivos(),
                        t.getDisponibilidad()
                ))
                .collect(Collectors.toList());
    }

    public void asignarCasoManual(String idTecnico) {
        Tecnico tecnico = tecnicoRepository.buscarPorId(idTecnico)
                .orElseThrow(() -> new RuntimeException("Técnico no encontrado"));

        // El dominio valida si tiene 5 casos y lanza la excepción si es necesario
        tecnico.asignarNuevoCaso();

        tecnicoRepository.guardar(tecnico);
    }

    public void registrarNuevoTecnico(service.dto.TecnicoCreacionDTO dto) {
        // Verifica si ya existe para evitar duplicados
        if (tecnicoRepository.buscarPorId(dto.getIdUsuario()).isPresent()) {
            throw new RuntimeException("Ya existe un técnico con el ID: " + dto.getIdUsuario());
        }

        Tecnico nuevoTecnico = new Tecnico(
                dto.getIdUsuario(),
                dto.getNombre(),
                dto.getDepartamento(),
                dto.getEspecialidad()
        );
        tecnicoRepository.guardar(nuevoTecnico);
    }
}