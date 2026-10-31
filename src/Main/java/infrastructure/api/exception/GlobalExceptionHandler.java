package infrastructure.api.exception;

import domain.exeptions.SobrecargaLaboralException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SobrecargaLaboralException.class)
    public ResponseEntity<Map<String, String>> handleSobrecargaLaboral(SobrecargaLaboralException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Conflicto de Asignación");
        response.put("mensaje", ex.getMessage());

        // Retorna HTTP 409 Conflict
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }
}
