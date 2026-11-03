package infrastructure.api.exception;

import domain.exeptions.SobrecargaLaboralException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleDatosObligatorios(IllegalArgumentException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, "Datos obligatorios", ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleConflicto(IllegalStateException ex) {
        return respuesta(HttpStatus.CONFLICT, "Conflicto", ex.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNoEncontrado(NoSuchElementException ex) {
        return respuesta(HttpStatus.NOT_FOUND, "No encontrado", ex.getMessage());
    }

    private ResponseEntity<Map<String, String>> respuesta(HttpStatus status, String error, String mensaje) {
        Map<String, String> body = new HashMap<>();
        body.put("error", error);
        body.put("mensaje", mensaje);
        return new ResponseEntity<>(body, status);
    }
}
