package domain.exeptions;

public class RecursoNoEncontradoException extends java.util.NoSuchElementException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
