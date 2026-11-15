package service.dto;

import java.time.LocalDate;
import java.util.List;

public record ReporteSemanalDTO(
        LocalDate inicioSemana,
        LocalDate finSemana,
        List<ReporteTecnicoSemanalDTO> tecnicos
) {}
