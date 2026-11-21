package service;

import domain.exeptions.DatosInvalidosException;

import java.util.regex.Pattern;

public final class ValidacionesEntrada {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private ValidacionesEntrada() {
    }

    public static String requerido(String valor, String campo, int longitudMaxima) {
        if (valor == null || valor.isBlank()) {
            throw new DatosInvalidosException("El campo " + campo + " es obligatorio.");
        }
        String limpio = valor.trim();
        if (limpio.length() > longitudMaxima) {
            throw new DatosInvalidosException("El campo " + campo + " no puede superar " + longitudMaxima + " caracteres.");
        }
        return limpio;
    }

    public static String email(String valor, String campo) {
        String limpio = requerido(valor, campo, 254);
        if (!EMAIL.matcher(limpio).matches()) {
            throw new DatosInvalidosException("El campo " + campo + " debe ser un correo electrónico válido.");
        }
        return limpio;
    }

    public static String opcional(String valor, String campo, int longitudMaxima) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String limpio = valor.trim();
        if (limpio.length() > longitudMaxima) {
            throw new DatosInvalidosException("El campo " + campo + " no puede superar " + longitudMaxima + " caracteres.");
        }
        return limpio;
    }
}
