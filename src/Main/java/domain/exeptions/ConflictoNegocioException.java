package domain.exeptions;

public class ConflictoNegocioException extends IllegalStateException {
    public ConflictoNegocioException(String mensaje) {
        super(mensaje);
    }
}
