package domain.exeptions;

public class SobrecargaLaboralException extends RuntimeException {
    public SobrecargaLaboralException(String mensaje) {
        super(mensaje);
    }
}