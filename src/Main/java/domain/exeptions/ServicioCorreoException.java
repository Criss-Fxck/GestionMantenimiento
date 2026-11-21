package domain.exeptions;

public class ServicioCorreoException extends RuntimeException {
    public ServicioCorreoException(String mensaje) {
        super(mensaje);
    }

    public ServicioCorreoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
