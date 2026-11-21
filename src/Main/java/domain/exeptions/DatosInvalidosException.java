package domain.exeptions;

public class DatosInvalidosException extends IllegalArgumentException {
    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}
