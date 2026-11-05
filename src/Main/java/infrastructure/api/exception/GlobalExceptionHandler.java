package infrastructure.api.exception;

import domain.exeptions.SobrecargaLaboralException;
import domain.exeptions.SolicitudInvalidaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SolicitudInvalidaException.class)
    public ResponseEntity<Object> handleSolicitudInvalida(SolicitudInvalidaException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, "Solicitud Inválida", ex.getMessage());
    }

    @ExceptionHandler(SobrecargaLaboralException.class)
    public ResponseEntity<Object> handleSobrecargaLaboral(SobrecargaLaboralException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Conflicto de Asignación");
        response.put("mensaje", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleDatosObligatorios(IllegalArgumentException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, "Datos obligatorios", ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> handleConflicto(IllegalStateException ex) {
        return respuesta(HttpStatus.CONFLICT, "Conflicto", ex.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Object> handleNoEncontrado(NoSuchElementException ex) {
        return respuesta(HttpStatus.NOT_FOUND, "No encontrado", ex.getMessage());
    }

    private ResponseEntity<Object> respuesta(HttpStatus status, String error, String mensaje) {
        Map<String, String> body = new HashMap<>();
        body.put("error", error);
        body.put("mensaje", mensaje);
        return new ResponseEntity<>(body, status);
    }
}