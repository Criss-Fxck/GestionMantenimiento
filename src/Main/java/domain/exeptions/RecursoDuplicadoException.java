package domain.exeptions;

public class RecursoDuplicadoException extends IllegalStateException {
    public RecursoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
